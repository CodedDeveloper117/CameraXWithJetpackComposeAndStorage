package com.coded.storagetutorial.ui.camera

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.coded.storagetutorial.storage.InternalStorage
import java.io.File
import java.text.SimpleDateFormat

@Composable
fun MainContent(modifier: Modifier = Modifier, storage: InternalStorage) {

    val emptyImageUri = Uri.parse("file:/dev/null")
    var imageUri by remember {
        mutableStateOf(emptyImageUri)
    }
    var imageFile by remember {
        mutableStateOf(File("/dev/null"))
    }
    if (imageUri != emptyImageUri) {
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = rememberImagePainter(imageUri),
                contentDescription = "CapturedImage",
                modifier = Modifier.fillMaxSize()
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Blue.copy(alpha = 0.4f))
                    .clickable {
                        imageUri = emptyImageUri
                    }
                    .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = "Delete",)
                }
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Blue.copy(alpha = 0.4f))
                    .clickable {
                        val bytes = imageFile.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        storage.savePhotoToInternalStorage(
                            System.currentTimeMillis().toString(),
                            bitmap
                        )
                    }
                    .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Save, contentDescription = "Save")
                }
            }
        }
    } else {
        CameraCapture(
            modifier = modifier,
            onImageFile = { file ->
                imageUri = file.toUri()
                imageFile = file
            }
        )
    }
}