package com.t8rin.compose_collage

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap

@Composable
fun CollageBox(
    modifier: Modifier = Modifier,
    pictures: List<PictureModel>,
    collageParams: CollageParams = CollageParams(
        hollowColor = MaterialTheme.colorScheme.primary,
        backgroundColor = Color.White
    ),
    shot: Boolean = false,
    onGetCollage: (Bitmap) -> Unit
) {
    var collageView: CollageView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            collageView = CollageView(context, true).apply {
                initPictureModelList(pictures)
                setGap(collageParams.gap)
                setBackgroundColor(collageParams.backgroundColor)
                setDragColor(collageParams.hollowColor)
                setHollowRoundRadius(collageParams.hollowRoundRadius)
            }
            collageView!!
        },
        update = {
            it.apply {
                initPictureModelList(pictures)
                setGap(collageParams.gap)
                setBackgroundColor(collageParams.backgroundColor)
                setDragColor(collageParams.hollowColor)
                setHollowRoundRadius(collageParams.hollowRoundRadius)
            }
            if (shot) onGetCollage(it.drawToBitmap())
        }
    )
}