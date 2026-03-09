-- Update IAM policy actions from documents:*Bucket* to buckets:*Bucket*
UPDATE revet_policy_statements
SET actions = REPLACE(actions, 'documents:ListBuckets', 'buckets:ListBuckets')
WHERE actions LIKE '%documents:ListBuckets%';

UPDATE revet_policy_statements
SET actions = REPLACE(actions, 'documents:GetBucket', 'buckets:GetBucket')
WHERE actions LIKE '%documents:GetBucket%';

UPDATE revet_policy_statements
SET actions = REPLACE(actions, 'documents:CreateBucket', 'buckets:CreateBucket')
WHERE actions LIKE '%documents:CreateBucket%';

UPDATE revet_policy_statements
SET actions = REPLACE(actions, 'documents:UpdateBucket', 'buckets:UpdateBucket')
WHERE actions LIKE '%documents:UpdateBucket%';

UPDATE revet_policy_statements
SET actions = REPLACE(actions, 'documents:DeleteBucket', 'buckets:DeleteBucket')
WHERE actions LIKE '%documents:DeleteBucket%';

-- Update resource URNs from urn:revet:documents:*:bucket/* to urn:revet:buckets:*:bucket/*
UPDATE revet_policy_statements
SET resources = REPLACE(resources, 'urn:revet:documents:', 'urn:revet:buckets:')
WHERE resources LIKE '%urn:revet:documents:%bucket%';
