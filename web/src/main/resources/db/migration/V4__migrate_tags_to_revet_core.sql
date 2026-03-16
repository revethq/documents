-- Migrate tag tables from Django taggit schema to revet-core schema

-- Step 1: Rename taggit_tag → revet_tags
ALTER TABLE IF EXISTS taggit_tag RENAME TO revet_tags;

-- Step 2: Add organization_id column to revet_tags
-- Populate from the first document's project's organization for each tag,
-- defaulting to 0 for orphan tags (tags not attached to any document).
ALTER TABLE revet_tags ADD COLUMN IF NOT EXISTS organization_id BIGINT;

UPDATE revet_tags t
SET organization_id = (
    SELECT p.organization_id
    FROM taggit_taggeditem ti
    JOIN revet_documents d ON d.id = ti.object_id AND ti.content_type_id = 1
    JOIN revet_projects p ON p.id = d.project_id
    WHERE ti.tag_id = t.id
    LIMIT 1
)
WHERE organization_id IS NULL;

-- Set any remaining tags (not attached to documents) to 0 as placeholder
UPDATE revet_tags SET organization_id = 0 WHERE organization_id IS NULL;

ALTER TABLE revet_tags ALTER COLUMN organization_id SET NOT NULL;

-- Drop unique constraints that were global (now scoped per org)
ALTER TABLE revet_tags DROP CONSTRAINT IF EXISTS taggit_tag_name_key;
ALTER TABLE revet_tags DROP CONSTRAINT IF EXISTS taggit_tag_slug_key;

-- Step 3: Migrate taggit_taggeditem → revet_tagged_items
-- Create new table with URN-based schema
CREATE TABLE IF NOT EXISTS revet_tagged_items (
    id BIGSERIAL PRIMARY KEY,
    tag_id INTEGER NOT NULL REFERENCES revet_tags(id),
    resource_urn VARCHAR(512) NOT NULL
);

-- Migrate existing tagged items (content_type_id=1 means Document)
-- Convert object_id to document URN using the document's UUID
INSERT INTO revet_tagged_items (tag_id, resource_urn)
SELECT ti.tag_id, 'urn:revet:documents::document/' || d.uuid
FROM taggit_taggeditem ti
JOIN revet_documents d ON d.id = ti.object_id
WHERE ti.content_type_id = 1;

-- Drop old table
DROP TABLE IF EXISTS taggit_taggeditem;
