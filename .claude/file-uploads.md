# Document Upload System - Frontend Integration Guide

## Overview

The document management system uses a **presigned URL workflow** for file uploads. Files are uploaded directly to S3/MinIO storage, bypassing the API server for better performance with large files.

## Upload Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Create     │     │  Initiate   │     │  Upload to  │     │  Complete   │
│  Document   │ ──▶ │  Upload     │ ──▶ │  S3 Direct  │ ──▶ │  Upload     │
│             │     │             │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
   POST               POST                PUT (S3)           PUT
   /documents         /files/             presigned URL      /document-versions/
                      initiate-upload                        {uuid}/complete-upload
```

## Step-by-Step Implementation

### 1. Create a Document (metadata container)

```typescript
const document = await documentsApi.createDocument({
  name: "Q4 Report",
  projectId: 123,
  categoryId: 456,        // optional
  mime: "application/pdf", // optional
  tags: ["reports", "2024"] // optional
});

// Save document.uuid for the next step
```

### 2. Initiate the Upload

```typescript
const uploadInfo = await filesApi.initiateUpload({
  documentUuid: document.uuid,
  fileName: file.name,
  contentType: file.type // optional, helps S3 set correct headers
});

// Response contains:
// - uploadUrl: presigned S3 PUT URL
// - s3Key: storage path
// - documentVersionUuid: version identifier
// - expiresInMinutes: URL validity period
```

### 3. Upload File Directly to S3

```typescript
// Upload directly to S3 using the presigned URL
const uploadResponse = await fetch(uploadInfo.uploadUrl, {
  method: 'PUT',
  body: file,
  headers: {
    'Content-Type': file.type
  }
});

if (!uploadResponse.ok) {
  throw new Error('Upload to storage failed');
}
```

### 4. Complete the Upload

```typescript
// Notify the API that upload is complete
// This verifies the file exists in storage and updates the version status
const version = await documentVersionsApi.completeUpload(
  uploadInfo.documentVersionUuid
);

// version.uploadStatus will be "COMPLETED"
```

## Download Flow

### Download Latest Version of a Document

```typescript
const downloadInfo = await documentsApi.downloadLatestVersion(document.uuid);

// Response contains:
// - downloadUrl: presigned S3 GET URL
// - expiresInMinutes: URL validity period
// - fileName: original file name

// Trigger download
window.open(downloadInfo.downloadUrl, '_blank');
```

## Upload Status States

| Status | Description |
|--------|-------------|
| `PENDING` | Upload initiated, waiting for file to be uploaded to S3 |
| `COMPLETED` | File verified in storage, ready for download |
| `FAILED` | Upload failed or verification failed |

## Error Handling

```typescript
try {
  const uploadInfo = await filesApi.initiateUpload({...});
} catch (error) {
  if (error.status === 404) {
    // Document not found
  } else if (error.status === 400) {
    // Storage not configured for this organization
  }
}
```

## Complete Example

```typescript
async function uploadDocument(
  projectId: number,
  file: File,
  options?: { categoryId?: number; tags?: string[] }
) {
  // 1. Create document metadata
  const document = await documentsApi.createDocument({
    name: file.name,
    projectId,
    categoryId: options?.categoryId,
    mime: file.type,
    tags: options?.tags ?? []
  });

  // 2. Get presigned upload URL
  const uploadInfo = await filesApi.initiateUpload({
    documentUuid: document.uuid,
    fileName: file.name,
    contentType: file.type
  });

  // 3. Upload to S3
  const uploadResponse = await fetch(uploadInfo.uploadUrl, {
    method: 'PUT',
    body: file,
    headers: { 'Content-Type': file.type }
  });

  if (!uploadResponse.ok) {
    throw new Error(`Upload failed: ${uploadResponse.statusText}`);
  }

  // 4. Mark upload complete
  const version = await documentVersionsApi.completeUpload(
    uploadInfo.documentVersionUuid
  );

  return { document, version };
}
```

## Notes

- **Presigned URLs expire** - default is 15 minutes. If upload takes longer, initiate a new upload.
- **Organization must have storage configured** - if not, initiate-upload returns 400 error.
- **Multiple versions** - each call to initiate-upload creates a new version. The download endpoint returns the latest completed version.
- **File size** - retrieved from S3 metadata when completing upload, no need to send from client.
