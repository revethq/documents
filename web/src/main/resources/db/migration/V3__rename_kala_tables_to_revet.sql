-- Rename legacy kala_ tables to revet_ prefix
ALTER TABLE IF EXISTS kala_documents RENAME TO revet_documents;
ALTER TABLE IF EXISTS kala_projects RENAME TO revet_projects;
ALTER TABLE IF EXISTS kala_companies RENAME TO revet_organizations;
ALTER TABLE IF EXISTS kala_document_version RENAME TO revet_document_versions;
