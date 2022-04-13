package com.coded.storagetutorial.storage

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.coded.storagetutorial.data.models.SharedStoragePhoto
import com.coded.storagetutorial.ui.utils.returnWithIODispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException

class ExternalStorage(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private var contentObserver: ContentObserver? = null

    val photos = mutableStateOf(emptyList<SharedStoragePhoto>())

    fun loadPhotos() {
        scope.launch {
            photos.value = loadImagesFromExternalStorage()
        }
    }


    fun registerObserver(): ContentObserver {

        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                loadPhotos()
            }
        }.also {
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                it
            )
        }
        return contentObserver!!
    }

    fun removeObserver() {
        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
        }

        contentObserver = null
    }

    suspend fun deletePhotoFromExternalStorage(uri: Uri, onSendIntentRequest: (IntentSenderRequest, Uri) -> Unit = {_, _ ->}) {
        returnWithIODispatchers {
            try {
                context.contentResolver.delete(uri, null, null)
            }catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(
                            context.contentResolver,
                            listOf(uri),
                        ).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    val intentSenderRequest = IntentSenderRequest.Builder(sender).build()
                    onSendIntentRequest(intentSenderRequest, uri)
                }
            }
        }
    }

    suspend fun savePhotoToExternalStorage(filename: String, bitmap: Bitmap): Boolean {
        return returnWithIODispatchers {

            val imageCollection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.WIDTH, bitmap.width)
                put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            }

            try {
                context.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    context.contentResolver.openOutputStream(uri).use { stream ->
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                            throw IOException("Couldn't Save Bitmap")
                        }
                    }
                } ?: throw IOException("Couldn't Create Media Store Entry")
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun loadImagesFromExternalStorage(): List<SharedStoragePhoto> {
        return returnWithIODispatchers {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projections = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )

            val photos = mutableListOf<SharedStoragePhoto>()

            context.contentResolver.query(
                collection,
                projections,
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(nameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(
                        SharedStoragePhoto(id, displayName, width, height, uri)
                    )
                }
                photos.toList()
            } ?: emptyList()
        }
    }
}

inline fun <T> sdk29AndUp(onSdk29: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdk29()
    } else null
}