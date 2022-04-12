package com.coded.storagetutorial.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import com.coded.storagetutorial.data.models.InternalStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class InternalStorage(
    private val context: Context
) {
    fun savePhotoToInternalStorage(filename: String, bitmap: Bitmap): Boolean {
        return try {
            context.openFileOutput("$filename.jpg", ComponentActivity.MODE_PRIVATE).use { outputStream ->
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Couldn't save bitmap")
                }
                true
            }
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            context.deleteFile(filename)
        }catch (e: IOException){
            e.printStackTrace()
            false
        }
    }

    suspend fun loadPhotoFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = context.filesDir.listFiles()
            files?.filter {
                it.canRead() && it.isFile && it.name.endsWith(".jpg")
            }?.map {
                val bytes = it.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bitmap)
            } ?: emptyList()
        }
    }
}