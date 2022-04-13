package com.coded.storagetutorial.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coded.storagetutorial.Screen
import com.coded.storagetutorial.data.models.InternalStoragePhoto
import com.coded.storagetutorial.storage.InternalStorage
import com.nesyou.staggeredgrid.LazyStaggeredGrid
import com.nesyou.staggeredgrid.StaggeredCells

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(navController: NavController, storage: InternalStorage) {

    var change by remember {
        mutableStateOf(false)
    }

    var photos by remember {
        mutableStateOf(emptyList<InternalStoragePhoto>())
    }

    LaunchedEffect(key1 = change) {
        photos = storage.loadPhotoFromInternalStorage()
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
        LazyStaggeredGrid(cells = StaggeredCells.Adaptive(minSize = 180.dp)) {
            items(photos.size) { index ->
                val random: Double = 100 + Math.random() * (500 - 100)
                val photo = photos[index]
                Image(
                    bitmap = photo.bitmap.asImageBitmap(),
                    contentDescription = photo.name,
                    modifier = Modifier.height(random.dp).padding(2.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if(photos.isEmpty()) {
                item {
                    Text(text = "Photos are empty")
                }
            }
        }
    }
}