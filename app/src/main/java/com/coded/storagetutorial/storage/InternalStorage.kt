package com.coded.storagetutorial.storage

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.coded.storagetutorial.data.models.InternalStoragePhoto
import com.coded.storagetutorial.data.models.SharedStoragePhoto
import com.coded.storagetutorial.ui.utils.returnWithIODispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class InternalStorage(
    private val context: Context,
    private val scope: CoroutineScope
) {

    val photos = mutableStateOf(emptyList<InternalStoragePhoto>())

    fun loadPhotos() {
        scope.launch {
            photos.value = loadPhotoFromInternalStorage()
        }
    }

    suspend fun savePhotoToInternalStorage(filename: String, bitmap: Bitmap): Boolean {
        return returnWithIODispatchers {
            try {
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
    }

    suspend fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return returnWithIODispatchers {
            try {
                val isSuccessful = context.deleteFile(filename)
                if(isSuccessful) {
                    loadPhotos()
                }
                isSuccessful
            }catch (e: IOException){
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun loadPhotoFromInternalStorage(): List<InternalStoragePhoto> {
        return returnWithIODispatchers {
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

