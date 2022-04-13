package com.coded.storagetutorial.ui

import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coded.storagetutorial.Screen
import com.coded.storagetutorial.storage.ExternalStorage
import com.coded.storagetutorial.storage.InternalStorage
import com.nesyou.staggeredgrid.LazyStaggeredGrid
import com.nesyou.staggeredgrid.StaggeredCells
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(
    navController: NavController,
    storage: InternalStorage,
    externalStorage: ExternalStorage,
    scope: CoroutineScope
) {

    val internalStoragePhotos by storage.photos

    val externalStoragePhotos by externalStorage.photos

    var imageUri: Uri? = null

    val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    scope.launch {
                        externalStorage.deletePhotoFromExternalStorage(imageUri ?: return@launch)
                    }
                }
            }
        }

    DisposableEffect(key1 = true) {
        externalStorage.registerObserver()
        externalStorage.loadPhotos()
        storage.registerObserver()
        storage.loadPhotos()
        onDispose {
            storage.removeObserver()
            externalStorage.removeObserver()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.Camera.route)
            }) {
                Icon(imageVector = Icons.Outlined.Camera, contentDescription = "Camera")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        ExternalStoragePermission {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                // My Books section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Internal Storage Images", fontSize = 26.sp, modifier = Modifier.padding(10.dp))
                        LazyRow {
                            renderInternalStoragePhotos(
                                internalStoragePhotos,
                                scope,
                                storage
                            )
                        }
                    }

                }
                item {
                    Text("External Storage Images", fontSize = 26.sp, modifier = Modifier.padding(10.dp))
                }
                renderExternalStoragePhotos(
                    externalStoragePhotos,
                    scope,
                    externalStorage,
                    changeImageUri = {
                        imageUri = it
                    },
                    launchRequest = {
                        launcher.launch(it)
                    }
                )
            }
        }
    }
}