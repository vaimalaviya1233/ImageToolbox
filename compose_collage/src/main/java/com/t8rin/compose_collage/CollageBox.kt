package com.t8rin.compose_collage

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap

@Composable
fun CollageBox(
    modifier: Modifier = Modifier,
    pictures: List<Uri>,
    collageParams: CollageParams = CollageParams(
        hollowColor = MaterialTheme.colorScheme.primary,
        backgroundColor = Color.White,
        collageLayoutType = CollageViewFactory.CollageLayoutType.THREE_IMAGE_0
    ),
    shot: Boolean = false,
    onGetCollage: (Bitmap) -> Unit
) {
    val size by remember {}
    AndroidView(
        modifier = modifier.onGloballyPositioned {

        },
        factory = { context ->
            CollageViewFactory(
                context,
                null,

                )
        },
        update = {

            if (shot) onGetCollage(it.drawToBitmap())
        }
    )
}