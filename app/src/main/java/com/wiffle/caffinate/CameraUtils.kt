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

    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun compressAndSaveImage(context: Context, sourceFile: File): File? {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

            options.inJustDecodeBounds = false
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            bitmap = rotateImageIfRequired(bitmap, sourceFile.absolutePath)

            val finalFile = createImageFile(context)

            FileOutputStream(finalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            bitmap.recycle()

            sourceFile.delete()

            return finalFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

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

    fun getFileSize(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    fun copyImageFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                return null
            }

            val tempFile = createImageFile(context)

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            val compressedFile = compressAndSaveImage(context, tempFile)

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
