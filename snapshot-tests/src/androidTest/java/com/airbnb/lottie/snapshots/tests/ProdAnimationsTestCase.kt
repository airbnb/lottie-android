package com.airbnb.lottie.snapshots.tests

import android.content.Context
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.snapshots.BuildConfig
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposition
import com.airbnb.lottie.snapshots.utils.await
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.S3ObjectSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ProdAnimationsTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() = coroutineScope {
        val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey)))
            .defaultBucket("lottie-prod-animations")
            .build()

        val allObjects = fetchAllObjects("lottie-prod-animations")

        val downloadChannel = downloadAnimations(context, allObjects, transferUtility)
        val compositionsChannel = parseCompositions(downloadChannel)
        repeat(1) {
            launch {
                for ((name, composition) in compositionsChannel) {
                    snapshotComposition(name, composition = composition)
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun CoroutineScope.parseCompositions(files: ReceiveChannel<File>) = produce(
        context = Dispatchers.Default,
        capacity = 50,
    ) {
        repeat(1) {
            launch {
                for (file in files) {
                    val result = if (file.name.endsWith("zip")) LottieCompositionFactory.fromZipStreamSync(ZipInputStream(FileInputStream(file)), file.name)
                    else LottieCompositionFactory.fromJsonInputStreamSync(FileInputStream(file), file.name)
                    val composition = result.value ?: throw IllegalStateException("Unable to parse ${file.nameWithoutExtension}", result.exception)
                    send("prod-${file.nameWithoutExtension}" to composition)
                }
            }
        }
    }

    private fun CoroutineScope.downloadAnimations(context: Context, animations: List<S3ObjectSummary>, transferUtility: TransferUtility) = produce(
        context = Dispatchers.IO,
        capacity = 30,
    ) {
        coroutineScope {
            animations
                .chunked(animations.size / 8)
                .forEach { animationsChunk ->
                    launch {
                        for (animation in animationsChunk) {
                            val file = File(context.cacheDir, animation.key)
                            file.deleteOnExit()
                            transferUtility.download(animation.key, file).await()
                            send(file)
                        }
                    }
                }
        }
    }

    private fun fetchAllObjects(bucket: String): List<S3ObjectSummary> {
        val allObjects = mutableListOf<S3ObjectSummary>()
        val s3Client = AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey))
        var request = ListObjectsV2Request().apply {
            bucketName = bucket
        }
        var result = s3Client.listObjectsV2(request)
        allObjects.addAll(result.objectSummaries)
        var startAfter = result.objectSummaries.lastOrNull()?.key
        while (startAfter != null) {
            request = ListObjectsV2Request().apply {
                bucketName = bucket
                this.startAfter = startAfter
            }
            result = s3Client.listObjectsV2(request)
            allObjects.addAll(result.objectSummaries)
            startAfter = result.objectSummaries.lastOrNull()?.key
        }
        return allObjects
    }
}