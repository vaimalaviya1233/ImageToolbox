package com.t8rin.compose_collage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Color as ComposeColor


@SuppressLint("ViewConstructor")
class CollageView(
    context: Context,
    private val isRegular: Boolean,
) : View(context) {

    companion object {
        private const val GAP_MAX = 20
        private const val ROUND_RADIUS_MAX = 10
        private const val PICTURE_ANIMATION_DELAY = 100L
        private const val SELECT_DRAG_RECT_LENGTH = 150
        private const val SELECT_DRAG_RECT_WIDTH = 20
    }

    private var mPictureModelList = mutableListOf<PictureModel>()

    private val mPictureHalfAlphaPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mHollowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mHollowSelectPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mMatrix = Matrix()

    private var mLastX: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()

    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()

    private var mLastFingerDistance: Double = 0.toDouble()
    private var mTouchPictureModel: PictureModel? = null

    private var backgroundBitmap: Bitmap? = null

    private var doubleTouchMode: Boolean = false

    private val viewConfig: ViewConfiguration
    private var downTime: Long = 0

    private var isNeedDrawShadow = false

    private val longClickHandler = Handler(Looper.getMainLooper())

    private var changePicMode = false
    private var willChangeModel: PictureModel? = null

    private var hollowGap = 0f
    private var lastPicGap = hollowGap

    private var hollowRoundRadius = 10.0f

    private var backgroundColor: Int = Color.TRANSPARENT


    fun initPictureModelList(pictureModelList: List<PictureModel>) {
        mPictureModelList.clear()
        mPictureModelList.addAll(pictureModelList)
        mPictureModelList.forEach {
            it.belongView = this
        }

        invalidate()
    }


    fun setGap(gap: Float) {
        val differGap = gap - lastPicGap
        mPictureModelList.forEach {
            val hollow = it.hollowModel
            hollow.hollowX = hollow.hollowX + differGap * GAP_MAX
            hollow.hollowY = hollow.hollowY + differGap * GAP_MAX
            hollow.width = hollow.width - differGap * GAP_MAX * 2
            hollow.height = hollow.height - differGap * GAP_MAX * 2
        }
        lastPicGap = gap
        invalidate()
    }

    fun setHollowRoundRadius(radius: Float) {
        this.hollowRoundRadius = radius * ROUND_RADIUS_MAX
        invalidate()
    }

    init {
        mHollowPaint.color = Color.TRANSPARENT
        mHollowPaint.strokeWidth = 2f
        mHollowPaint.style = Paint.Style.STROKE

        mHollowSelectPaint.color = Color.TRANSPARENT
        mHollowSelectPaint.strokeWidth = 10f
        mHollowSelectPaint.style = Paint.Style.STROKE

        mPictureHalfAlphaPaint.alpha = 80

        viewConfig = ViewConfiguration.get(context)
    }

    fun setDragColor(color: ComposeColor) {
        mHollowPaint.color = color.toArgb()
        mHollowSelectPaint.color = color.toArgb()
        invalidate()
    }

    fun setBackgroundColor(color: ComposeColor) {
        backgroundColor = color.toArgb()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(backgroundColor)
        drawBackground(canvas)
        drawPicture(canvas)
    }

    private fun drawPicture(canvas: Canvas?) {
        canvas?.let {
            mPictureModelList.forEach {
                canvas.save()

                val scaleX = it.scaleX
                val scaleY = it.scaleY

                val bitmap = it.bitmapPicture

                val hollowModel = it.hollowModel
                val hollowX = hollowModel.hollowX
                val hollowY = hollowModel.hollowY
                val hollowWidth = hollowModel.width
                val hollowHeight = hollowModel.height
                val hollowPath = hollowModel.path

                if (isRegular && hollowPath == null) {
                    val rect = RectF(0f, 0f, hollowWidth, hollowHeight)

                    if (!it.isSelected || !changePicMode) {
                        Log.d("CollageView", "setPictureXToHollowCenter: ${it.xToHollowCenter}")
                        Log.d("CollageView", "setPictureYToHollowCenter: ${it.yToHollowCenter}")

                        canvas.translate(hollowX, hollowY)

                        val pictureX = hollowWidth / 2 - bitmap.width / 2 + it.xToHollowCenter
                        val pictureY = hollowHeight / 2 - bitmap.height / 2 + it.yToHollowCenter

                        mMatrix.postTranslate(pictureX, pictureY)
                        mMatrix.postScale(
                            scaleX,
                            scaleY,
                            (hollowWidth / 2 + it.xToHollowCenter),
                            (hollowHeight / 2 + it.yToHollowCenter)
                        )

                        mMatrix.postRotate(
                            it.rotateDegree,
                            (hollowWidth / 2 + it.xToHollowCenter),
                            (hollowHeight / 2 + it.yToHollowCenter)
                        )

                        val clipPath = Path()
                        clipPath.addRoundRect(
                            rect,
                            hollowRoundRadius,
                            hollowRoundRadius,
                            Path.Direction.CW
                        )
                        canvas.clipPath(clipPath)

                        if (changePicMode && !it.isSelected && it == willChangeModel) {
                            canvas.drawBitmap(bitmap, mMatrix, mPictureHalfAlphaPaint)
                        } else {
                            canvas.drawBitmap(bitmap, mMatrix, null)
                        }
                    }

                    mMatrix.reset()
                    canvas.restore()

                    // drawHollowWithDragSign(it, canvas)
                } else {
                    if (!it.isSelected || !changePicMode) {
                        val scalePathGap = getPathScale()
                        canvas.scale(
                            scalePathGap,
                            scalePathGap,
                            it.hollowModel.centerPoint!!.x.toFloat(),
                            it.hollowModel.centerPoint.y.toFloat()
                        )
                        canvas.clipPath(hollowPath!!)
                        canvas.scale(
                            1 / scalePathGap,
                            1 / scalePathGap,
                            it.hollowModel.centerPoint.x.toFloat(),
                            it.hollowModel.centerPoint.y.toFloat()
                        )
                        canvas.translate(hollowX, hollowY)

                        val pictureX = hollowWidth / 2 - bitmap.width / 2 + it.xToHollowCenter
                        val pictureY = hollowHeight / 2 - bitmap.height / 2 + it.yToHollowCenter

                        mMatrix.postTranslate(pictureX, pictureY)
                        //    scalePicWithGap(hollowWidth, it, hollowHeight)
                        mMatrix.postScale(
                            scaleX,
                            scaleY,
                            (hollowWidth / 2 + it.xToHollowCenter),
                            (hollowHeight / 2 + it.yToHollowCenter)
                        )
                        mMatrix.postRotate(
                            it.rotateDegree,
                            (hollowWidth / 2 + it.xToHollowCenter),
                            (hollowHeight / 2 + it.yToHollowCenter)
                        )

                        if (changePicMode && !it.isSelected && it == willChangeModel) {
                            canvas.drawBitmap(bitmap, mMatrix, mPictureHalfAlphaPaint)
                        } else {
                            canvas.drawBitmap(bitmap, mMatrix, null)
                        }
                    }

                    mMatrix.reset()
                    canvas.restore()

                    if (it.isSelected) {
                        canvas.save()
                        val scalePath = getPathScale()
                        canvas.scale(
                            scalePath,
                            scalePath,
                            it.hollowModel.centerPoint!!.x.toFloat(),
                            it.hollowModel.centerPoint.y.toFloat()
                        )
                        canvas.drawPath(hollowPath!!, mHollowSelectPaint)
                        canvas.restore()
                    }
                }
            }

            mTouchPictureModel?.let {
                drawHollowWithDragSign(it, canvas)
            }

            drawPictureShadow(canvas)
        }

    }

    private fun drawHollowWithDragSign(it: PictureModel, canvas: Canvas) {
        if (!isRegular) {
            return
        }
        if (it.isSelected) {
            val rect = RectF(
                it.hollowModel.hollowX,
                it.hollowModel.hollowY,
                it.hollowModel.hollowX + it.hollowModel.width,
                it.hollowModel.hollowY + it.hollowModel.height
            )
            canvas.drawRoundRect(rect, hollowRoundRadius, hollowRoundRadius, mHollowSelectPaint)

            val canDragList = it.getCanDragList()
            canDragList.forEach { direction ->
                when (direction) {
                    HollowModel.LEFT -> {
                        val rectLeft = RectF(
                            (it.hollowModel.hollowX - SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height / 2 - SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.hollowX + SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height / 2 + SELECT_DRAG_RECT_LENGTH / 2)
                        )
                        canvas.drawRoundRect(
                            rectLeft,
                            hollowRoundRadius,
                            hollowRoundRadius,
                            mHollowSelectPaint
                        )
                    }

                    HollowModel.TOP -> {
                        val rectLeft = RectF(
                            (it.hollowModel.hollowX + it.hollowModel.width / 2 - SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowY - SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowX + it.hollowModel.width / 2 + SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowY + SELECT_DRAG_RECT_WIDTH / 2)
                        )
                        canvas.drawRoundRect(
                            rectLeft,
                            hollowRoundRadius,
                            hollowRoundRadius,
                            mHollowSelectPaint
                        )
                    }

                    HollowModel.RIGHT -> {
                        val rectLeft = RectF(
                            (it.hollowModel.hollowX + it.hollowModel.width - SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height / 2 - SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowX + it.hollowModel.width + SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height / 2 + SELECT_DRAG_RECT_LENGTH / 2)
                        )
                        canvas.drawRoundRect(
                            rectLeft,
                            hollowRoundRadius,
                            hollowRoundRadius,
                            mHollowSelectPaint
                        )

                    }

                    HollowModel.BOTTOM -> {
                        val rectLeft = RectF(
                            (it.hollowModel.hollowX + it.hollowModel.width / 2 - SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height - SELECT_DRAG_RECT_WIDTH / 2),
                            (it.hollowModel.hollowX + it.hollowModel.width / 2 + SELECT_DRAG_RECT_LENGTH / 2),
                            (it.hollowModel.hollowY + it.hollowModel.height + SELECT_DRAG_RECT_WIDTH / 2)
                        )
                        canvas.drawRoundRect(
                            rectLeft,
                            hollowRoundRadius,
                            hollowRoundRadius,
                            mHollowSelectPaint
                        )
                    }
                }
            }
        }
    }

    private fun getPathScale(): Float {
        return 1 - lastPicGap * 0.2f
    }

    fun overTurnHorizontal() {
        mTouchPictureModel?.overTurnHorizontal()
        invalidate()
    }

    fun overTurnVertical() {
        mTouchPictureModel?.overTurnVertical()
        invalidate()
    }

    fun setRotateDegree(degree: Float) {
        mTouchPictureModel?.let {
            it.rotateDegree = degree
            Log.d("CollageView", "rotateDegree: ${it.rotateDegree}")
        }

        invalidate()
    }

    private fun drawPictureShadow(canvas: Canvas?) {
        if (!changePicMode) {
            return
        }
        mTouchPictureModel?.let {
            if (!it.isSelected || !isNeedDrawShadow) {
                return
            }

            canvas?.save()

            val scaleX = it.scaleX
            val scaleY = it.scaleY

            val bitmap = it.bitmapPicture

            val hollowModel = it.hollowModel
            val hollowX = hollowModel.hollowX
            val hollowY = hollowModel.hollowY
            val hollowWidth = hollowModel.width
            val hollowHeight = hollowModel.height

            canvas?.translate(hollowX, hollowY)
            val pictureX = hollowWidth / 2 - bitmap.width / 2 + it.xToHollowCenter
            val pictureY = hollowHeight / 2 - bitmap.height / 2 + it.yToHollowCenter

            mMatrix.postTranslate(pictureX, pictureY)
            //scalePicWithGap(hollowWidth, it, hollowHeight)
            mMatrix.postScale(
                scaleX,
                scaleY,
                (hollowWidth / 2 + it.xToHollowCenter),
                (hollowHeight / 2 + it.yToHollowCenter)
            )
            mMatrix.postRotate(
                it.rotateDegree,
                (hollowWidth / 2 + it.xToHollowCenter),
                (hollowHeight / 2 + it.yToHollowCenter)
            )

            canvas?.drawBitmap(bitmap, mMatrix, mPictureHalfAlphaPaint)
            canvas?.restore()
        }
        mMatrix.reset()
    }

    private fun drawBackground(canvas: Canvas?) {
        backgroundBitmap?.let { backgroundBitmap ->
            val scale = getCenterPicScale(backgroundBitmap, width, height)
            mMatrix.setScale(scale, scale)
            canvas?.let {
                it.save()
                it.translate((width / 2).toFloat(), (height / 2).toFloat())
                it.drawBitmap(backgroundBitmap, mMatrix, null)
                it.restore()
                mMatrix.reset()
            }
        }
    }

    private var refreshLastEventListener: (MotionEvent) -> Unit = { event ->
        mLastX = event.x
        mLastY = event.y
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()

                mLastX = event.x
                mLastY = event.y

                mDownX = event.x
                mDownY = event.y

                Log.d("CollageView", "mLastX:$mLastX")
                Log.d("CollageView", "mLastY:$mLastY")

                val tempModel = getTouchPicModel(event)
                longClickHandler.postDelayed({
                    mTouchPictureModel = tempModel
                    selectPictureModel()
                    changePicMode = true
                    invalidate()
                }, 600L)

                mTouchPictureModel?.refreshIsTouchHollowState(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {

                    isNeedDrawShadow = true
                    doubleTouchMode = true

                    if (mTouchPictureModel != null) {
                        mLastFingerDistance = distanceBetweenFingers(event)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val distanceFromDownPoint = getDisFromDownPoint(event)
                if (distanceFromDownPoint > viewConfig.scaledTouchSlop) {
                    longClickHandler.removeCallbacksAndMessages(null)
                }
                isNeedDrawShadow = true
                when (event.pointerCount) {
                    1 -> {
                        if (doubleTouchMode) {
                            return true
                        }

                        val dx = (event.x - mLastX).toInt()
                        val dy = (event.y - mLastY).toInt()

                        Log.d("CollageView", "HollowModel dy:$dy")

                        mTouchPictureModel?.let { pictureModel ->
                            if (pictureModel.isTouchHollow) {
                                if (pictureModel.handleHollowDrag(
                                        event,
                                        dx,
                                        dy,
                                        true,
                                        refreshLastEventListener
                                    )
                                ) {
                                    invalidate()
                                }
                                return true
                            }
                        }

                        mTouchPictureModel?.let {

                            if (changePicMode) {
                                willChangeModel = getTouchPicModel(event)
                            }

                            it.xToHollowCenter = it.xToHollowCenter + dx
                            it.yToHollowCenter = it.yToHollowCenter + dy
                            invalidate()
                        }

                        mLastX = event.x
                        mLastY = event.y
                    }

                    2 -> {
                        mTouchPictureModel?.let {
                            val fingerDistance = distanceBetweenFingers(event)
                            val scaleRatioDelta =
                                fingerDistance.toFloat() / mLastFingerDistance.toFloat()

                            Log.d("CollageView", "scaleRatioDelta:$scaleRatioDelta")

                            val tempScaleX = scaleRatioDelta * it.scaleX
                            val tempScaleY = scaleRatioDelta * it.scaleY

                            if (abs(tempScaleX) < 3 || abs(tempScaleX) > 0.5) {
                                it.scaleX = tempScaleX
                                it.scaleY = tempScaleY

                                invalidate()
                                mLastFingerDistance = fingerDistance
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longClickHandler.removeCallbacksAndMessages(null)

                if (changePicMode && mTouchPictureModel != null && willChangeModel != null && mTouchPictureModel != willChangeModel) {
                    val tempBitmap = mTouchPictureModel!!.bitmapPicture
                    mTouchPictureModel!!.bitmapPicture = willChangeModel!!.bitmapPicture
                    willChangeModel!!.bitmapPicture = tempBitmap
                    mTouchPictureModel!!.refreshStateWhenChangePic()
                    willChangeModel!!.refreshStateWhenChangePic()
                }

                changePicMode = false
                willChangeModel = null

                val distanceFromDownPoint = getDisFromDownPoint(event)
                if (distanceFromDownPoint < viewConfig.scaledTouchSlop) {
                    mTouchPictureModel = getTouchPicModel(event)
                    selectPictureModel()
                    invalidate()
                    isNeedDrawShadow = false
                    return true
                }

                isNeedDrawShadow = false

                if (doubleTouchMode) {
                    mTouchPictureModel?.let {
                        it.backToCenterCropState(it, false)
                    }

                } else {
                    mTouchPictureModel?.let {
                        if (!it.isTouchHollow) {
                            it.translatePictureCropHollowByAnimationIfNeed()
                        } else {
                            postDelayed({
                                it.backToCenterCropState(it, true)
                            }, PICTURE_ANIMATION_DELAY + 10)
                        }
                    }
                }

                mTouchPictureModel?.cancelHollowTouch(mTouchPictureModel!!)

                if (doubleTouchMode) {
                    doubleTouchMode = false
                }

                invalidate()
            }
        }

        return true
    }

    private fun selectPictureModel() {
        mPictureModelList.forEach {
            it.isSelected = false
        }
        mTouchPictureModel?.isSelected = true
    }

    private fun getTouchPicModel(event: MotionEvent): PictureModel? {
        when (event.pointerCount) {
            1 -> {
                val x = event.x
                val y = event.y
                Log.d("CollageView", "getTouchPicModel x:$x y:$y")
                for (picModel in mPictureModelList) {

                    if (isRegular) {
                        val hollowX = picModel.hollowModel.hollowX
                        val hollowY = picModel.hollowModel.hollowY
                        val hollowWidth = picModel.hollowModel.width
                        val hollowHeight = picModel.hollowModel.height

                        val rect =
                            RectF(hollowX, hollowY, hollowX + hollowWidth, hollowY + hollowHeight)
                        if (rect.contains(x, y)) {
                            return picModel
                        }
                    } else {
                        val path = picModel.hollowModel.path
                        val re = Region()
                        val r = RectF()
                        path?.let {
                            it.computeBounds(r, true)
                            re.setPath(
                                it,
                                Region(
                                    r.left.toInt(),
                                    r.top.toInt(),
                                    r.right.toInt(),
                                    r.bottom.toInt()
                                )
                            )
                            if (re.contains(x.toInt(), y.toInt())) {
                                return picModel
                            }
                        }
                    }

                }
            }

            2 -> {
                val x0 = event.getX(0)
                val y0 = event.getY(0)
                val x1 = event.getX(1)
                val y1 = event.getY(1)
                for (picModel in mPictureModelList) {
                    val hollowX = picModel.hollowModel.hollowX
                    val hollowY = picModel.hollowModel.hollowY
                    val hollowWidth = picModel.hollowModel.width
                    val hollowHeight = picModel.hollowModel.height

                    val rect =
                        RectF(hollowX, hollowY, hollowX + hollowWidth, hollowY + hollowHeight)
                    if (rect.contains(x0, y0) || rect.contains(x1, y1)) {
                        return picModel
                    }
                }
            }

            else -> {}
        }
        return null
    }

    fun setBackground(background: Bitmap) {
        backgroundBitmap = background
        invalidate()
    }

    private fun getCenterPicScale(bitmap: Bitmap, width: Int, height: Int): Float {
        val widthBmp = bitmap.width
        val heightBmp = bitmap.height
        val scale = if (widthBmp < heightBmp) {
            width / widthBmp.toFloat()
        } else {
            height / heightBmp.toFloat()
        }

        matrix.setScale(scale, scale)
        return scale
    }

    private fun distanceBetweenFingers(event: MotionEvent): Double {
        val disX = abs(event.getX(0) - event.getX(1))
        val disY = abs(event.getY(0) - event.getY(1))
        return sqrt((disX * disX + disY * disY).toDouble())
    }

    private fun getDisFromDownPoint(event: MotionEvent): Double {
        val disX = abs(event.x - mDownX)
        val disY = abs(event.y - mDownY)
        return sqrt((disX * disX + disY * disY).toDouble())
    }

}