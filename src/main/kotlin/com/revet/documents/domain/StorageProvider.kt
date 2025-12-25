package com.revet.documents.domain

/**
 * Supported cloud storage providers for document storage.
 */
enum class StorageProvider {
    S3,
    GCS,
    AZURE_BLOB,
    MINIO
}
