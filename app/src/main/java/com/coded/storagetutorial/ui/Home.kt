package com.coded.storagetutorial.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.coded.storagetutorial.data.models.InternalStoragePhoto

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(
    list: List<InternalStoragePhoto>,
    onCameraClick: () -> Unit
) {
    LazyVerticalGrid(cells = GridCells.Fixed(2)) {
        items(list.size) { index ->
            Image(
                bitmap = list[index].bitmap.asImageBitmap(),
                contentDescription = list[index].name
            )
        }
        item {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(10.dp)
            ) {
                IconButton(onClick = { onCameraClick() }) {
                    Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "none")
                }
            }
        }
    }
}