package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Service managing photo and asset storage for MedTrack.
 *
 * Supports:
 * - Direct upload to Firebase Cloud Storage with public download URLs
 * - Robust local fallback into app's private files directory for offline use or when Cloud Storage is unavailable
 * - Automatic image compression before upload to conserve bandwidth and storage
 */
object FirebaseStorageService {

    /**
     * Uploads a medication image to Firebase Storage (or stores locally if Storage is unavailable/offline).
     * Returns either a public Firebase Storage HTTPS URL or a local persistent file URI.
     */
    suspend fun uploadMedicationImage(
        context: Context,
        sourceUri: Uri,
        hubId: String,
        medicationId: String = UUID.randomUUID().toString()
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Read and compress bitmap from source URI
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                // If decoding fails, return sourceUri as fallback string
                return@withContext sourceUri.toString()
            }

            // Downscale bitmap if too large (max 1024px width/height)
            val scaledBitmap = scaleBitmapToMaxDimension(originalBitmap, 1024)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()

            // 2. Always persist a local copy in internal storage so it is permanently accessible on device
            val localFolder = File(context.filesDir, "medications").apply { if (!exists()) mkdirs() }
            val localFile = File(localFolder, "med_${medicationId}.jpg")
            try {
                val fos = FileOutputStream(localFile)
                fos.write(imageBytes)
                fos.flush()
                fos.close()
            } catch (_: Exception) {}

            val localFileUriString = Uri.fromFile(localFile).toString()

            // 3. Attempt Firebase Storage upload
            val storage = try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseStorage.getInstance()
                } else null
            } catch (_: Exception) {
                null
            }

            if (storage != null) {
                try {
                    val storageRef = storage.reference
                        .child("family_hubs")
                        .child(hubId)
                        .child("medications")
                        .child("${medicationId}.jpg")

                    val uploadTask = storageRef.putBytes(imageBytes).await()
                    val downloadUri = storageRef.downloadUrl.await()
                    return@withContext downloadUri.toString()
                } catch (e: Exception) {
                    // Firebase Storage failed (e.g. offline, security rules, quota) -> return permanent local file URI
                    return@withContext localFileUriString
                }
            }

            return@withContext localFileUriString
        } catch (e: Exception) {
            return@withContext sourceUri.toString()
        }
    }

    /**
     * Uploads an avatar / profile image.
     */
    suspend fun uploadProfileImage(
        context: Context,
        sourceUri: Uri,
        userId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close() ?: return@withContext sourceUri.toString()

            if (originalBitmap == null) return@withContext sourceUri.toString()

            val scaledBitmap = scaleBitmapToMaxDimension(originalBitmap, 512)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()

            val localFolder = File(context.filesDir, "profiles").apply { if (!exists()) mkdirs() }
            val localFile = File(localFolder, "profile_${userId}.jpg")
            try {
                val fos = FileOutputStream(localFile)
                fos.write(imageBytes)
                fos.flush()
                fos.close()
            } catch (_: Exception) {}

            val localFileUriString = Uri.fromFile(localFile).toString()

            val storage = try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseStorage.getInstance()
                } else null
            } catch (_: Exception) {
                null
            }

            if (storage != null) {
                try {
                    val storageRef = storage.reference
                        .child("users")
                        .child(userId)
                        .child("avatar.jpg")

                    storageRef.putBytes(imageBytes).await()
                    val downloadUri = storageRef.downloadUrl.await()
                    return@withContext downloadUri.toString()
                } catch (_: Exception) {
                    return@withContext localFileUriString
                }
            }

            return@withContext localFileUriString
        } catch (_: Exception) {
            return@withContext sourceUri.toString()
        }
    }

    private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
