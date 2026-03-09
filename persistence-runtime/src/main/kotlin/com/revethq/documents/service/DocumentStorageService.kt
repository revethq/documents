package com.revethq.documents.service

import com.revethq.buckets.domain.FileMetadata
import com.revethq.buckets.domain.PresignedUrl
import com.revethq.buckets.service.StorageService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class DocumentStorageService
    @Inject
    constructor(
        private val storageService: StorageService,
    ) {
        fun generatePresignedUploadUrl(
            bucketId: Long,
            key: String,
            contentType: String?,
        ): PresignedUrl = storageService.generatePresignedUploadUrl(bucketId, key, contentType)

        fun generatePresignedDownloadUrl(
            bucketId: Long,
            key: String,
        ): PresignedUrl = storageService.generatePresignedDownloadUrl(bucketId, key)

        fun checkFileExists(
            bucketId: Long,
            key: String,
        ): Boolean = storageService.checkFileExists(bucketId, key)

        fun getFileMetadata(
            bucketId: Long,
            key: String,
        ): FileMetadata = storageService.getFileMetadata(bucketId, key)

        fun deleteFile(
            bucketId: Long,
            key: String,
        ): Boolean = storageService.deleteFile(bucketId, key)

        fun generateStorageKey(
            documentId: Long,
            fileName: String,
        ): String {
            val sanitizedFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val uuid = UUID.randomUUID().toString()
            return "documents/$documentId/$uuid-$sanitizedFileName"
        }
    }
