package com.t8rin.compose_collage

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class PictureModel(
    var bitmapPicture: Bitmap,
    val hollowModel: HollowModel = HollowModel(0f, 0f, 1000f, 1000f),
    var xToHollowCenter: Int = 0,
    var yToHollowCenter: Int = 0
) {

    companion object {
        private const val HOLLOW_TOUCH_WIDTH = 50
        private const val HOLLOW_SCALE_UPPER_LIMIT = 2
        private const val HOLLOW_TOUCH_LOWER_LIMIT = 100
        private const val PICTURE_ANIMATION_DELAY = 100L
    }

    var belongView: View? = null
    private var initScale: Float
    var scaleX: Float
    var scaleY: Float

    private val mEffectPictureModel: SparseArray<SparseArray<List<PictureModel>>> = SparseArray()

    private val mCanDragDirectionList = mutableListOf<Int>()

    var isSelected: Boolean = false

    var isTouchHollow = false
    var rotateDegree = 0f

    private var overTurnHorizontal = 1f
    private var overTurnVertical = 1f

    init {
        val hollowWidth = hollowModel.width
        val hollowHeight = hollowModel.height
        initScale = getCenterPicScale(bitmapPicture, hollowWidth, hollowHeight)
        scaleX = initScale
        scaleY = initScale

    }

    fun getCanDragList(): List<Int> {
        return mCanDragDirectionList
    }

    fun initCanDragDirectionList() {
        if (mEffectPictureModel.get(HollowModel.LEFT) != null) {
            mCanDragDirectionList.add(HollowModel.LEFT)
        }
        if (mEffectPictureModel.get(HollowModel.TOP) != null) {
            mCanDragDirectionList.add(HollowModel.TOP)
        }
        if (mEffectPictureModel.get(HollowModel.RIGHT) != null) {
            mCanDragDirectionList.add(HollowModel.RIGHT)
        }
        if (mEffectPictureModel.get(HollowModel.BOTTOM) != null) {
            mCanDragDirectionList.add(HollowModel.BOTTOM)
        }
    }

    fun refreshStateWhenChangePic() {
        val hollowWidth = hollowModel.width
        val hollowHeight = hollowModel.height
        initScale = getCenterPicScale(bitmapPicture, hollowWidth, hollowHeight)
        scaleX = initScale * overTurnHorizontal
        scaleY = initScale * overTurnVertical
        xToHollowCenter = 0
        yToHollowCenter = 0
    }

    /**
     * 拖动当前model的currentModelDirection方向边，会带动modelList的model的targetDirection边变化
     * @param currentModelDirection：当前model的移动方向边
     */
    fun addEffectPictureModel(
        modelArray: SparseArray<List<PictureModel>>,
        currentModelDirection: Int
    ) {
        mEffectPictureModel.put(currentModelDirection, modelArray)
    }

    private fun setScaleWithCondition(value: Float) {
        if (abs(scaleX * value) < initScale || abs(scaleY * value) < initScale) {
            scaleX = initScale * overTurnHorizontal
            scaleY = initScale * overTurnVertical
            return
        }
        if (abs(scaleX * value) > HOLLOW_SCALE_UPPER_LIMIT * initScale) {
            return
        }
        scaleX *= value
        scaleY *= value
    }

    /**
     * 得到在固定的显示尺寸限定得Bitmap显示centerCrop效果的缩放比例(scale为图片和边框宽高比最大的值)
     */
    private fun getCenterPicScale(bitmap: Bitmap, width: Float, height: Float): Float {
        val widthBmp = bitmap.width
        val heightBmp = bitmap.height
        val widthScale = width / widthBmp.toFloat()
        val heightScale = height / heightBmp.toFloat()
        return if (widthScale < heightScale) {
            heightScale
        } else {
            widthScale
        }

    }

    /**
     * 刷新是否触摸到边框状态（即边框可拖动）
     */
    fun refreshIsTouchHollowState(event: MotionEvent) {
        val x = event.x
        val y = event.y

        val hollowX = hollowModel.hollowX
        val hollowY = hollowModel.hollowY
        val hollowWidth = hollowModel.width
        val hollowHeight = hollowModel.height

        val rectLeft = RectF(
            hollowX - HOLLOW_TOUCH_WIDTH,
            hollowY,
            hollowX + HOLLOW_TOUCH_WIDTH,
            hollowY + hollowHeight
        )
        val rectTop = RectF(
            hollowX,
            hollowY - HOLLOW_TOUCH_WIDTH,
            hollowX + hollowWidth,
            hollowY + HOLLOW_TOUCH_WIDTH
        )
        val rectRight = RectF(
            hollowX + hollowWidth - HOLLOW_TOUCH_WIDTH,
            hollowY,
            hollowX + hollowWidth + HOLLOW_TOUCH_WIDTH,
            hollowY + hollowHeight
        )
        val rectBottom = RectF(
            hollowX,
            hollowY + hollowHeight - HOLLOW_TOUCH_WIDTH,
            hollowX + hollowWidth,
            hollowY + hollowHeight + HOLLOW_TOUCH_WIDTH
        )

        //点在矩形区域中
        if (rectLeft.contains(x, y) && mEffectPictureModel.get(HollowModel.LEFT) != null) {
            hollowModel.selectSide = HollowModel.LEFT
            isTouchHollow = true
        }
        if (rectTop.contains(x, y) && mEffectPictureModel.get(HollowModel.TOP) != null) {
            hollowModel.selectSide = HollowModel.TOP
            isTouchHollow = true
        }
        if (rectRight.contains(x, y) && mEffectPictureModel.get(HollowModel.RIGHT) != null) {
            hollowModel.selectSide = HollowModel.RIGHT
            isTouchHollow = true
        }
        if (rectBottom.contains(x, y) && mEffectPictureModel.get(HollowModel.BOTTOM) != null) {
            hollowModel.selectSide = HollowModel.BOTTOM
            isTouchHollow = true
        }

    }

    /**
     * 处理边框拖动事件
     * return：边框有没有有效移动
     */
    fun handleHollowDrag(
        event: MotionEvent,
        dx: Int,
        dy: Int,
        needEffectOthers: Boolean,
        overRangeListener: (MotionEvent) -> Unit
    ): Boolean {
        hollowModel.let { model ->
            when (model.selectSide) {
                HollowModel.LEFT -> {
                    val width = model.width - dx
                    if (width < HOLLOW_TOUCH_LOWER_LIMIT) {
                        //超出范围就不作处理

                        //使用回调函数
                        overRangeListener.invoke(event)
                        return false
                    }

                    //联动其他的PictureModel
                    if (needEffectOthers) {
                        if (!handleEffectPictureModel(
                                HollowModel.LEFT,
                                event,
                                dx,
                                dy,
                                overRangeListener
                            )
                        ) {
                            //表示有一个联动的model已经到了最小值，不能再拖动边框
                            return false
                        }
                    }
                    val lastWidth = model.width
                    model.width = width
                    model.hollowX = model.hollowX + dx
                    setScaleWithCondition((model.width / lastWidth))

                }

                HollowModel.RIGHT -> {
                    val width = model.width + dx
                    if (width < HOLLOW_TOUCH_LOWER_LIMIT) {
                        //超出范围就不作处理

                        //使用回调函数
                        overRangeListener.invoke(event)
                        return false
                    }
                    //联动其他的PictureModel
                    if (needEffectOthers) {
                        if (!handleEffectPictureModel(
                                HollowModel.RIGHT,
                                event,
                                dx,
                                dy,
                                overRangeListener
                            )
                        ) {
                            //表示有一个联动的model已经到了最小值，不能再拖动边框
                            return false
                        }
                    }
                    val lastWidth = model.width
                    model.width = model.width + dx
                    setScaleWithCondition((model.width / lastWidth))
                }

                HollowModel.TOP -> {
                    val height = model.height - dy
                    if (height < HOLLOW_TOUCH_LOWER_LIMIT) {
                        //超出范围就不作处理

                        //使用回调函数
                        overRangeListener.invoke(event)
                        return false
                    }

                    if (needEffectOthers) {
                        if (!handleEffectPictureModel(
                                HollowModel.TOP,
                                event,
                                dx,
                                dy,
                                overRangeListener
                            )
                        ) {
                            return false
                        }
                    }
                    val lastHeight = model.height
                    model.height = height
                    model.hollowY = model.hollowY + dy
                    setScaleWithCondition((model.height / lastHeight))

                }

                HollowModel.BOTTOM -> {
                    val height = model.height + dy
                    if (height < HOLLOW_TOUCH_LOWER_LIMIT) {
                        overRangeListener.invoke(event)
                        return false
                    }

                    if (needEffectOthers) {
                        if (!handleEffectPictureModel(
                                HollowModel.BOTTOM,
                                event,
                                dx,
                                dy,
                                overRangeListener
                            )
                        ) {
                            return false
                        }
                    }

                    val lastHeight = model.height
                    model.height = height

                    setScaleWithCondition((model.height / lastHeight))

                    Log.d("CollageView", "HollowModel.height:${model.height}")
                    Log.d("CollageView", "HollowModel dy:$dy")
                }

                else -> {
                    return false
                }
            }

            makePictureCropHollowWithoutAnimationIfNeed()
            overRangeListener.invoke(event)
        }

        return true
    }

    private fun handleEffectPictureModel(
        currentDirection: Int,
        event: MotionEvent,
        dx: Int,
        dy: Int,
        overRangeListener: (MotionEvent) -> Unit
    ): Boolean {
        val modelArray = mEffectPictureModel.get(currentDirection)
        modelArray?.let { array ->
            when (currentDirection) {
                HollowModel.LEFT -> {
                    val canDrag = handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.RIGHT,
                        overRangeListener
                    )
                    if (!canDrag) {
                        return false
                    }
                    handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.LEFT,
                        overRangeListener
                    )
                }

                HollowModel.TOP -> {
                    val canDrag = handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.BOTTOM,
                        overRangeListener
                    )
                    if (!canDrag) {
                        return false
                    }
                    handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.TOP,
                        overRangeListener
                    )
                }

                HollowModel.RIGHT -> {
                    val canDrag = handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.LEFT,
                        overRangeListener
                    )
                    if (!canDrag) {
                        return false
                    }
                    handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.RIGHT,
                        overRangeListener
                    )
                }

                HollowModel.BOTTOM -> {
                    val canDrag = handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.TOP,
                        overRangeListener
                    )
                    if (!canDrag) {
                        return false
                    }
                    handleEffectPicForOneDirection(
                        array,
                        event,
                        dx,
                        dy,
                        HollowModel.BOTTOM,
                        overRangeListener
                    )
                }

                else -> {

                }

            }
        }
        return true
    }

    private fun handleEffectPicForOneDirection(
        array: SparseArray<List<PictureModel>>, event: MotionEvent, dx: Int, dy: Int,
        direction: Int, overRangeListener: (MotionEvent) -> Unit
    ): Boolean {
        val modelList = array.get(direction)
        modelList?.forEach {
            it.hollowModel.selectSide = direction
            if (!it.handleHollowDrag(event, dx, dy, false, overRangeListener)) {
                return false
            }
        }

        return true
    }

    fun cancelHollowTouch(pictureModel: PictureModel) {
        pictureModel.isTouchHollow = false
        pictureModel.hollowModel.selectSide = HollowModel.NO_SIDE

        val canHollowTouch = { model: PictureModel ->
            model.isTouchHollow = false
            model.hollowModel.selectSide = HollowModel.NO_SIDE
        }
        handleAllPictureModelByAction(canHollowTouch)
    }


    private fun handleAllPictureModelByAction(action: (PictureModel) -> Unit) {
        val arraySize = mEffectPictureModel.size()
        for (i in 0 until arraySize) {
            val keyArray = mEffectPictureModel.keyAt(i)
            val modelArray = mEffectPictureModel.get(keyArray)
            val modelSize = modelArray.size()
            for (j in 0 until modelSize) {
                val keyList = modelArray.keyAt(j)
                val modelList = modelArray.get(keyList)
                modelList.forEach {
                    action.invoke(it)
                }
            }
        }
    }


    private fun makePictureCropHollowWithoutAnimationIfNeed() {

        val hollowModel = hollowModel
        val bitmap = bitmapPicture
        val hollowX = hollowModel.hollowX
        val hollowY = hollowModel.hollowY
        val hollowWidth = hollowModel.width
        val hollowHeight = hollowModel.height

        val pictureLeft =
            (hollowX + xToHollowCenter + hollowWidth / 2 - bitmap.width / 2 * abs(scaleX))
        val pictureTop =
            (hollowY + yToHollowCenter + hollowHeight / 2 - bitmap.height / 2 * abs(scaleY))
        val pictureRight =
            (hollowX + xToHollowCenter + hollowWidth / 2 + bitmap.width / 2 * abs(scaleX))
        val pictureBottom =
            (hollowY + yToHollowCenter + hollowHeight / 2 + bitmap.height / 2 * abs(scaleY))

        val leftDiffer = pictureLeft - hollowX
        val topDiffer = pictureTop - hollowY
        val rightDiffer = pictureRight - (hollowX + hollowWidth)
        val bottomDiffer = pictureBottom - (hollowY + hollowHeight)

        if (leftDiffer > 0) {
            val targetXToHollow = (xToHollowCenter - leftDiffer).toInt()

            xToHollowCenter = targetXToHollow

            Log.d("CollageView", "targetXToHollow:$targetXToHollow")
        }
        if (topDiffer > 0) {
            val targetYToHollow = (yToHollowCenter - topDiffer).toInt()
            yToHollowCenter = targetYToHollow

            Log.d("CollageView", "targetYToHollow:$targetYToHollow")
        }
        if (rightDiffer < 0) {
            val targetXToHollow = (xToHollowCenter - rightDiffer).toInt()
            xToHollowCenter = targetXToHollow

            Log.d("CollageView", "targetXToHollow:$targetXToHollow")
        }

        if (bottomDiffer < 0) {
            val targetYToHollow = (yToHollowCenter - bottomDiffer).toInt()

            yToHollowCenter = targetYToHollow

            Log.d("CollageView", "targetYToHollow: $targetYToHollow")
        }

    }

    fun backToCenterCropState(pictureModel: PictureModel, needEffectOthers: Boolean) {
        pictureModel.let {
            backToCenterCrop(it)

            if (!needEffectOthers) {
                return
            }
            val centerCrop = { model: PictureModel ->
                model.backToCenterCrop(model)
            }
            handleAllPictureModelByAction(centerCrop)
        }
    }

    private fun backToCenterCrop(it: PictureModel) {
        val hollowX = it.hollowModel.hollowX
        val hollowY = it.hollowModel.hollowY
        val hollowWidth = it.hollowModel.width
        val hollowHeight = it.hollowModel.height

        val pictureLeft =
            (hollowX + it.xToHollowCenter + hollowWidth / 2 - it.bitmapPicture.width / 2 * abs(
                it.scaleX
            ))
        val pictureTop =
            (hollowY + it.yToHollowCenter + hollowHeight / 2 - it.bitmapPicture.height / 2 * abs(
                it.scaleY
            ))
        val pictureRight =
            (hollowX + it.xToHollowCenter + hollowWidth / 2 + it.bitmapPicture.width / 2 * abs(
                it.scaleX
            ))
        val pictureBottom =
            (hollowY + it.yToHollowCenter + hollowHeight / 2 + it.bitmapPicture.height / 2 * abs(
                it.scaleY
            ))

        val leftDiffer = pictureLeft - hollowX
        val topDiffer = pictureTop - hollowY
        val rightDiffer = pictureRight - (hollowX + hollowWidth)
        val bottomDiffer = pictureBottom - (hollowY + hollowHeight)

        if (leftDiffer > 0 || topDiffer > 0 || rightDiffer < 0 || bottomDiffer < 0) {
            val targetScale = getCenterPicScale(it.bitmapPicture, hollowWidth, hollowHeight)
            startAnimation("PictureScale", it.scaleX, targetScale)
            // startAnimation("PictureScale", it.scaleY, targetScale)
            startAnimation("PictureXToHollowCenter", it.xToHollowCenter, 0)
            startAnimation("PictureYToHollowCenter", it.yToHollowCenter, 0)
        }
    }

    fun translatePictureCropHollowByAnimationIfNeed() {
        val hollowModel = hollowModel
        val bitmap = bitmapPicture
        val hollowX = hollowModel.hollowX
        val hollowY = hollowModel.hollowY
        val hollowWidth = hollowModel.width
        val hollowHeight = hollowModel.height

        val pictureLeft =
            (hollowX + xToHollowCenter + hollowWidth / 2 - bitmap.width / 2 * abs(scaleX))
        val pictureTop =
            (hollowY + yToHollowCenter + hollowHeight / 2 - bitmap.height / 2 * abs(scaleY))
        val pictureRight =
            (hollowX + xToHollowCenter + hollowWidth / 2 + bitmap.width / 2 * abs(scaleX))
        val pictureBottom =
            (hollowY + yToHollowCenter + hollowHeight / 2 + bitmap.height / 2 * abs(scaleY))

        val leftDiffer = pictureLeft - hollowX
        val topDiffer = pictureTop - hollowY
        val rightDiffer = pictureRight - (hollowX + hollowWidth)
        val bottomDiffer = pictureBottom - (hollowY + hollowHeight)

        if (leftDiffer > 0) {
            val targetXToHollow = (xToHollowCenter - leftDiffer).toInt()

            startAnimation("PictureXToHollowCenter", xToHollowCenter, targetXToHollow)
        }
        if (topDiffer > 0) {
            val targetYToHollow = (yToHollowCenter - topDiffer).toInt()

            startAnimation("PictureYToHollowCenter", yToHollowCenter, targetYToHollow)
        }

        if (rightDiffer < 0) {
            val targetXToHollow = (xToHollowCenter - rightDiffer).toInt()

            startAnimation("PictureXToHollowCenter", xToHollowCenter, targetXToHollow)

            Log.d("CollageView", "targetXToHollow:$targetXToHollow")
        }

        if (bottomDiffer < 0) {
            val targetYToHollow = (yToHollowCenter - bottomDiffer).toInt()

            startAnimation("PictureYToHollowCenter", yToHollowCenter, targetYToHollow)
        }

    }

    private fun startAnimation(propertyName: String, initValue: Int, targetValue: Int) {
        val animator = ObjectAnimator.ofInt(this, propertyName, initValue, targetValue)
        animator.duration = PICTURE_ANIMATION_DELAY
        animator.start()
    }

    private fun startAnimation(propertyName: String, initValue: Float, targetValue: Float) {
        val animator = ObjectAnimator.ofFloat(this, propertyName, initValue, targetValue)
        animator.duration = PICTURE_ANIMATION_DELAY
        animator.start()
    }

    fun setPictureXToHollowCenter(x: Int) {
        xToHollowCenter = x
        belongView?.invalidate()

        Log.d("PictureModel", "setPictureXToHollowCenter: $x")
    }


    fun setPictureYToHollowCenter(y: Int) {
        yToHollowCenter = y
        belongView?.invalidate()

        Log.d("PictureModel", "setPictureYToHollowCenter: $y")
    }


    fun setPictureScale(scale: Float) {
//        this.scaleX = scale*overTurnHorizontal
//        this.scaleY = scale*overTurnVertical
        this.scaleX = scale
        this.scaleY = scale
        belongView?.invalidate()

        Log.d("PictureModel", "setScaleX: $scale")
    }

    fun overTurnHorizontal() {
        overTurnHorizontal *= -1
        scaleX *= overTurnHorizontal
    }

    fun overTurnVertical() {
        overTurnVertical *= -1
        scaleY *= overTurnVertical
    }

    fun getBitmap(): Bitmap = bitmapPicture

}
