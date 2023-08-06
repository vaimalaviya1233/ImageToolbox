package com.t8rin.compose_collage

import androidx.compose.ui.graphics.Color

data class CollageParams(
    val hollowColor: Color,
    val backgroundColor: Color,
    val hollowRoundRadius: Float = 10.0f,
    val gap: Float = 0f,
    val collageLayoutType: CollageViewFactory.CollageLayoutType
)