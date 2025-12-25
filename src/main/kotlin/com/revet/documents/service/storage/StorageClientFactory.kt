package com.revet.documents.service.storage

import com.revet.documents.domain.Bucket
import com.revet.documents.domain.StorageProvider
import jakarta.enterprise.context.ApplicationScoped

/**
 * Factory for creating StorageProviderClient instances based on Bucket configuration.
 */
interface StorageClientFactory {
    fun createClient(bucket: Bucket): StorageProviderClient
}

@ApplicationScoped
class StorageClientFactoryImpl : StorageClientFactory {

    override fun createClient(bucket: Bucket): StorageProviderClient {
        return when (bucket.provider) {
            StorageProvider.S3, StorageProvider.MINIO -> S3StorageProviderClient(bucket)
            StorageProvider.GCS -> GcsStorageProviderClient(bucket)
            StorageProvider.AZURE_BLOB -> throw UnsupportedOperationException("Azure Blob provider not yet implemented")
        }
    }
}
