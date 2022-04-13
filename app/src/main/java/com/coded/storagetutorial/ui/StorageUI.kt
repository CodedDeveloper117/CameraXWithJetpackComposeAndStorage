package com.coded.storagetutorial.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.coded.storagetutorial.Screen
import com.coded.storagetutorial.data.models.InternalStoragePhoto
import com.coded.storagetutorial.data.models.SharedStoragePhoto
import com.coded.storagetutorial.storage.ExternalStorage
import com.coded.storagetutorial.storage.InternalStorage
import com.coded.storagetutorial.ui.camera.Rationale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.nesyou.staggeredgrid.LazyStaggeredGrid
import com.nesyou.staggeredgrid.StaggeredCells
import com.nesyou.staggeredgrid.StaggeredGridScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.Internal

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExternalStoragePermission(
    content: @Composable () -> Unit,
) {

    val readPermission = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    val writePermission = rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    var hasWritePermission by remember { mutableStateOf(minSdk29) }

    LaunchedEffect(key1 = writePermission.hasPermission) {
        hasWritePermission = writePermission.hasPermission || minSdk29
    }


    Log.d("TAG", "${readPermission.hasPermission} for storage ui")

    if (!readPermission.hasPermission) {
        Rationale(
            text = "Read Storage Request",
            onRequestPermission = {
                readPermission.launchPermissionRequest()
            }
        )
    }

    if (!writePermission.hasPermission && !minSdk29) {
        Rationale(
            text = "Write Storage Request",
            onRequestPermission = {
                readPermission.launchPermissionRequest()
            }
        )
    }

    if (readPermission.permissionRequested && !readPermission.hasPermission && !hasWritePermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No Access to External Storage")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {

            }) {
                Text("Request Permissions")
            }
        }
    }

    if (readPermission.hasPermission && hasWritePermission) {
        content()
    }

}

fun LazyListScope.renderExternalStoragePhotos(
    photos: List<SharedStoragePhoto>,
    scope: CoroutineScope,
    storage: ExternalStorage,
    changeImageUri: (Uri) -> Unit,
    launchRequest: (IntentSenderRequest) -> Unit,
) {
    val windowed = photos.windowed(2, 2, true)
    items(windowed) { row ->
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            row.forEach { photo ->
                val random: Double = 100 + (0.5 * (500 - 100))
                Image(
                    rememberImagePainter(photo.contentUri),
                    contentDescription = photo.name,
                    modifier = Modifier
                        .weight(1f)
                        .height(random.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { /* Called when the gesture starts */ },
                                onLongPress = {
                                    scope.launch {
                                        storage.deletePhotoFromExternalStorage(photo.contentUri) { request, uri ->
                                            changeImageUri(uri)
                                            launchRequest(request)
                                        }
                                    }
                                },
                            )
                        }
                        .padding(2.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
    if (photos.isEmpty()) {
        item {
            Text(text = "Photos are empty")
        }
    }
}


fun LazyListScope.renderInternalStoragePhotos(
    photos: List<InternalStoragePhoto>,
    scope: CoroutineScope,
    storage: InternalStorage,
) {
    items(photos.size) { index ->
        val height: Double = 100 + (0.7 * (500 - 100))
        val width: Double = 100 + (0.4 * (500 - 100))
        val photo = photos[index]
        Image(
            bitmap = photo.bitmap.asImageBitmap(),
            contentDescription = photo.name,
            modifier = Modifier
                .height(height.dp)
                .width(width.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { /* Called when the gesture starts */ },
                        onLongPress = {
                            scope.launch {
                                scope.launch {
                                    storage.deletePhotoFromInternalStorage(photo.name)
                                }
                            }
                        },
                    )
                }
                .padding(2.dp),
            contentScale = ContentScale.Crop
        )
    }
    if (photos.isEmpty()) {
        item {
            Text(text = "Photos are empty")
        }
    }
}
