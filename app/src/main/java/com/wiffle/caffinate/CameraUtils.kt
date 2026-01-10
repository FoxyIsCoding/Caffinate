package com.wiffle.caffinate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {

    /**
     * Creates a temporary file for storing the captured image
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "CAFFINATE_${timeStamp}"
        val storageDir = context.getExternalFilesDir("Pictures")

        if (storageDir?.exists() == false) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    /**
     * Creates a content URI for the image file using FileProvider
     */
    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Compresses and saves the image to reduce storage size
     */
    fun compressAndSaveImage(context: Context, sourceFile: File): File? {
        try {
            // Decode the image to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

            // Decode with inSampleSize
            options.inJustDecodeBounds = false
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // Rotate bitmap if needed based on EXIF data
            bitmap = rotateImageIfRequired(bitmap, sourceFile.absolutePath)

            // Create final file
            val finalFile = createImageFile(context)

            // Compress and save
            FileOutputStream(finalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            bitmap.recycle()

            // Delete the original file if compression was successful
            sourceFile.delete()

            return finalFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Calculate sample size for bitmap decoding to reduce memory usage
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Rotate image based on EXIF orientation data
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, imagePath: String): Bitmap {
        try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return bitmap
        }
    }

    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        bitmap.recycle()
        return rotatedBitmap
    }

    /**
     * Delete an image file
     */
    fun deleteImageFile(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get file size in a human-readable format
     */
    fun getFileSize(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Copy image from URI (gallery picker) to app's private storage
     * and compress it
     */
    fun copyImageFromUri(context: Context, uri: Uri): File? {
        return try {
            // Open input stream from the URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                return null
            }

            // Create a temporary file to save the image
            val tempFile = createImageFile(context)

            // Copy the stream to the temp file
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Now compress and save the image
            val compressedFile = compressAndSaveImage(context, tempFile)

            // Return the compressed file (temp file is already deleted in compressAndSaveImage)
            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
