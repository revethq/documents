-- Rename kala_buckets table to revet_buckets (metadata-only operation in PostgreSQL)
ALTER TABLE IF EXISTS kala_buckets RENAME TO revet_buckets;
