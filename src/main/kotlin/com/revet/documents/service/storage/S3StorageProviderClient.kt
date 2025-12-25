package com.revet.documents.service.storage

import com.revet.documents.domain.Bucket
import com.revet.documents.service.FileMetadata
import com.revet.documents.service.PresignedUrl
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration

/**
 * S3/MinIO implementation of StorageProviderClient.
 * Creates clients dynamically based on Bucket configuration.
 */
class S3StorageProviderClient(
    private val bucket: Bucket
) : StorageProviderClient {

    private val credentials = AwsBasicCredentials.create(bucket.accessKey, bucket.secretKey)
    private val credentialsProvider = StaticCredentialsProvider.create(credentials)
    private val region = Region.of(bucket.region ?: "us-east-1")

    private val s3Client: S3Client by lazy {
        S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .apply {
                bucket.endpoint?.let { endpointOverride(URI.create(it)) }
            }
            .build()
    }

    private val s3Presigner: S3Presigner by lazy {
        S3Presigner.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .apply {
                bucket.endpoint?.let { endpointOverride(URI.create(it)) }
            }
            .build()
    }

    override fun generatePresignedUploadUrl(key: String, contentType: String?): PresignedUrl {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket.bucketName)
            .key(key)
            .apply {
                contentType?.let { contentType(it) }
            }
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(bucket.presignedUrlDurationMinutes.toLong()))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)

        return PresignedUrl(
            url = presignedRequest.url().toString(),
            expiresInMinutes = bucket.presignedUrlDurationMinutes,
            s3Key = key
        )
    }

    override fun generatePresignedDownloadUrl(key: String): PresignedUrl {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket.bucketName)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(bucket.presignedUrlDurationMinutes.toLong()))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)

        return PresignedUrl(
            url = presignedRequest.url().toString(),
            expiresInMinutes = bucket.presignedUrlDurationMinutes,
            s3Key = key
        )
    }

    override fun exists(key: String): Boolean {
        return try {
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket.bucketName)
                .key(key)
                .build()
            s3Client.headObject(headObjectRequest)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getFileMetadata(key: String): FileMetadata {
        val headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucket.bucketName)
            .key(key)
            .build()

        val response = s3Client.headObject(headObjectRequest)

        return FileMetadata(
            size = response.contentLength(),
            contentType = response.contentType(),
            lastModified = response.lastModified()
        )
    }

    override fun delete(key: String): Boolean {
        return try {
            s3Client.deleteObject { builder ->
                builder.bucket(bucket.bucketName).key(key)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
