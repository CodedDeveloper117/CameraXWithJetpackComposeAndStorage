package com.coded.storagetutorial.ui.camera

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coded.storagetutorial.Screen
import com.coded.storagetutorial.storage.ExternalStorage
import com.coded.storagetutorial.storage.InternalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    storage: InternalStorage,
    scope: CoroutineScope,
    externalStorage: ExternalStorage
) {

    val emptyImageUri = Uri.parse("file:/dev/null")
    val context = LocalContext.current
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
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Red.copy(alpha = 0.4f))
                    .clickable {
                        val bytes = imageFile.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        scope.launch {
                            val isSuccessful = externalStorage.savePhotoToExternalStorage(
                                System
                                    .currentTimeMillis()
                                    .toString(),
                                bitmap
                            )
                            if (isSuccessful) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            } else {
                                Toast
                                    .makeText(context, "Not Successful", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save to External Storage", color = Color.White)
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Blue.copy(alpha = 0.4f))
                        .clickable {
                            val bytes = imageFile.readBytes()
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            scope.launch {
                                val isSuccessful = storage.savePhotoToInternalStorage(
                                    System
                                        .currentTimeMillis()
                                        .toString(),
                                    bitmap
                                )
                                if (isSuccessful) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                } else {
                                    Toast
                                        .makeText(context, "Not Successful", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    } else {
        CameraCapture(
            modifier = modifier,
            onImageFile = { file ->
                imageUri = file.toUri()
                imageFile = file
            },
        )
    }
}