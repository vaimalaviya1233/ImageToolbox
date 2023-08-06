package com.t8rin.compose_collage

import android.graphics.Path
import android.graphics.Point

data class HollowModel(
    var hollowX: Float,
    var hollowY: Float,
    var initWidth: Float,
    var initHeight: Float,
    val path: Path? = null,
    val centerPoint: Point? = null
) {
    var width: Float = initWidth
    var height: Float = initHeight

    companion object {
        const val NO_SIDE = -1
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
    }

    var selectSide: Int = NO_SIDE
}