package ru.versoit.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom view for displaying a clock.
 *
 * This view provides customizable properties for text color, container color,
 * second hand color, minute hand color, hour hand color and clock background
 *
 * @param context The context in which the view is created.
 * @param attrs The attribute set containing custom attributes.
 * @param defStyleAttr The default style attribute.
 */
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class HandDimensions(
        val offsetRatio: Float,
        val widthRatio: Float,
        val heightRatio: Float,
    )

    private data class Measurements(
        val centerX: Float = 0f,
        val centerY: Float = 0f,
        val clockFieldSize: Float = 0f,
        val clockFieldCenter: Float = 0f,
        val textPositionsOnCircle: List<Pair<Float, Float>> = emptyList(),
    )

    @ColorInt
    var textColor: Int

    @ColorInt
    var colorContainer: Int

    @ColorInt
    var secondHandColor: Int

    @ColorInt
    var minuteHandColor: Int

    @ColorInt
    var hourHandColor: Int

    private var pausedTime: Long = 0

    private var clockBackground: Drawable? = null

    private val minuteHandDrawable = AppCompatResources.getDrawable(context, R.drawable.minute_hand)
    private val hourHandDrawable = AppCompatResources.getDrawable(context, R.drawable.hour_hand)

    private var cachedMeasurements = Measurements()

    private val paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textAlignment = TEXT_ALIGNMENT_CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    var paused: Boolean = false
        set(pause) {
            if (!paused && pause) {
                pausedTime = getTimeInMillis()
            } else if (paused && !pause) {
                requestLayout()
            }
            field = pause
        }

    @DrawableRes
    var clockShape: Int = R.drawable.clock_shape
        set(value) {
            clockBackground = ContextCompat.getDrawable(context, value)
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)

        textColor = typedArray.getColor(R.styleable.ClockView_android_textColor, Color.BLACK)
        colorContainer = typedArray.getColor(R.styleable.ClockView_colorContainer, Color.WHITE)
        clockShape = typedArray.getResourceId(
            R.styleable.ClockView_shape,
            R.drawable.clock_shape
        )
        secondHandColor = typedArray.getColor(
            R.styleable.ClockView_secondHandColor,
            ContextCompat.getColor(context, R.color.second_hand)
        )
        minuteHandColor = typedArray.getColor(
            R.styleable.ClockView_minuteHandColor,
            ContextCompat.getColor(context, R.color.minute_hand)
        )
        hourHandColor = typedArray.getColor(
            R.styleable.ClockView_hourHandColor,
            ContextCompat.getColor(context, R.color.hour_hand)
        )

        typedArray.recycle()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        val centerX = width / 2f
        val centerY = height / 2f

        val clockFieldSize = min(width, height).toFloat()
        val clockFieldCenter = clockFieldSize / 2f

        paint.textSize = TEXT_RATIO * clockFieldSize
        cachedMeasurements = Measurements(
            centerX = centerX,
            centerY = centerY,
            clockFieldSize = clockFieldSize,
            clockFieldCenter = clockFieldCenter,
            textPositionsOnCircle = generateTextPositionsOnCircle(
                centerX = centerX,
                centerY = centerY,
                paint = paint,
                radius = clockFieldCenter.toInt().percent(90),
            )
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val width = calculateSize(
            mode = widthMode,
            size = widthSize,
            desiredSize = DESIRED_WIDTH
        )
        val height =
            calculateSize(
                mode = heightMode,
                size = heightSize,
                desiredSize = DESIRED_HEIGHT
            )
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)
        drawCircleContainer(canvas, paint)
        drawNumbers(canvas, paint)

        val time = getTimeInMillis()
        drawHourHand(canvas, time)
        drawMinuteHand(canvas, time)
        drawSecondHand(canvas, time)

        if (!paused) {
            invalidate()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return ClockViewState(
            superState = superState,
            secondHandColor = secondHandColor,
            minuteHandColor = minuteHandColor,
            hourHandColor = hourHandColor,
            textColor = textColor,
            colorContainer = colorContainer,
            clockShape = clockShape,
            paused = paused,
            pausedTime = pausedTime,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ClockViewState) {
            secondHandColor = state.secondHandColor
            minuteHandColor = state.minuteHandColor
            hourHandColor = state.hourHandColor
            textColor = state.textColor
            colorContainer = state.colorContainer
            clockShape = state.clockShape
            paused = state.paused
            pausedTime = state.pausedTime
        }
        super.onRestoreInstanceState(state)
    }

    private fun drawBackground(canvas: Canvas) {
        val centerX = cachedMeasurements.centerX.toInt()
        val centerY = cachedMeasurements.centerY.toInt()
        val clockFieldCenter = cachedMeasurements.clockFieldCenter.toInt()
        clockBackground?.setBounds(
            centerX - clockFieldCenter,
            centerY - clockFieldCenter,
            centerX + clockFieldCenter,
            centerY + clockFieldCenter
        )
        clockBackground?.draw(canvas)
    }

    private fun drawCircleContainer(canvas: Canvas, paint: Paint) {
        paint.color = colorContainer
        canvas.drawCircle(
            cachedMeasurements.centerX,
            cachedMeasurements.centerY,
            cachedMeasurements.clockFieldSize.toInt().percent(90) / 2f,
            paint
        )
    }

    private fun drawNumbers(canvas: Canvas, paint: Paint) {

        if (cachedMeasurements.textPositionsOnCircle.size != PARTS_OF_CLOCK_AMOUNT) {
            throw RuntimeException("Incorrect number of positions on the clock face")
        }

        paint.color = textColor
        for (index in 0 until PARTS_OF_CLOCK_AMOUNT) {
            canvas.drawText(
                "${index + 1}",
                cachedMeasurements.textPositionsOnCircle[index].first,
                cachedMeasurements.textPositionsOnCircle[index].second,
                paint
            )
        }
    }

    private fun drawSecondHand(canvas: Canvas, time: Long) {
        paint.color = secondHandColor
        drawHand(
            offsetRatio = SECOND_HAND_OFFSET_RATIO,
            widthRatio = SECOND_HAND_WIDTH_RATIO,
            heightRatio = SECOND_HAND_HEIGHT_RATIO,
            time = time,
            millisInOneCircle = 60_000L,
        ) { left, top, right, bottom, rotation ->
            canvas.save()
            canvas.rotate(rotation, cachedMeasurements.centerX, cachedMeasurements.centerY)
            canvas.drawRect(
                left,
                top,
                right,
                bottom,
                paint,
            )
            canvas.restore()
        }
    }

    private fun drawMinuteHand(canvas: Canvas, time: Long) {
        drawHandWithDrawableResource(
            HandDimensions(
                offsetRatio = MINUTE_HAND_OFFSET_RATIO,
                widthRatio = MINUTE_HAND_WIDTH_RATIO,
                heightRatio = MINUTE_HAND_HEIGHT_RATIO,
            ),
            drawable = minuteHandDrawable,
            canvas = canvas,
            millisInOneCircle = 3_600_000L,
            color = minuteHandColor,
            time = time
        )
    }

    private fun drawHourHand(canvas: Canvas, time: Long) {
        drawHandWithDrawableResource(
            HandDimensions(
                offsetRatio = HOUR_HAND_OFFSET_RATIO,
                widthRatio = HOUR_HAND_WIDTH_RATIO,
                heightRatio = HOUR_HAND_HEIGHT_RATIO,
            ),
            drawable = hourHandDrawable,
            canvas = canvas,
            millisInOneCircle = 43_200_000L,
            color = hourHandColor,
            time = time
        )
    }

    private fun drawHandWithDrawableResource(
        handDimensions: HandDimensions,
        drawable: Drawable?,
        color: Int,
        canvas: Canvas,
        millisInOneCircle: Long,
        time: Long,
    ) {
        drawHand(
            handDimensions.offsetRatio,
            handDimensions.widthRatio,
            handDimensions.heightRatio,
            time = time,
            millisInOneCircle = millisInOneCircle,
        ) { left, top, right, bottom, rotation ->
            canvas.save()
            canvas.rotate(rotation, cachedMeasurements.centerX, cachedMeasurements.centerY)
            drawable?.setTint(color)
            drawable?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            drawable?.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawHand(
        offsetRatio: Float,
        widthRatio: Float,
        heightRatio: Float,
        time: Long,
        millisInOneCircle: Long,
        onDraw: (left: Float, top: Float, right: Float, bottom: Float, rotation: Float) -> Unit
    ) {
        val timeOfClock = time % millisInOneCircle

        val rectWidth = cachedMeasurements.clockFieldSize * widthRatio
        val rectHeight = cachedMeasurements.clockFieldSize * heightRatio
        val offset = cachedMeasurements.clockFieldSize * offsetRatio

        val rotationDegrees =
            timeOfClock.toFloat() / millisInOneCircle * DEGREES_IN_CIRCLE - DEGREES_START_OFFSET

        onDraw(
            cachedMeasurements.centerX - offset,
            cachedMeasurements.centerY - rectHeight / 2,
            cachedMeasurements.centerX - offset + rectWidth,
            cachedMeasurements.centerY + rectHeight / 2,
            rotationDegrees
        )
    }

    private fun calculateSize(mode: Int, size: Int, desiredSize: Int): Int {
        return when (mode) {
            MeasureSpec.AT_MOST -> min(desiredSize, size)
            MeasureSpec.EXACTLY -> size
            else -> desiredSize
        }
    }

    private fun generateTextPositionsOnCircle(
        centerX: Float,
        centerY: Float,
        paint: Paint,
        radius: Int,
    ): List<Pair<Float, Float>> {
        val positions = mutableListOf<Pair<Float, Float>>()
        val angleIncrement = 2 * Math.PI / PARTS_OF_CLOCK_AMOUNT
        for (index in 1..PARTS_OF_CLOCK_AMOUNT) {
            val number = index.toString()

            val angle = angleIncrement * index - Math.PI / 2
            val textHeight = getTextBounds(number, paint).height()
            val x = centerX + (radius - textHeight) * cos(angle).toFloat()
            val y = centerY + (radius - textHeight) * sin(angle).toFloat()

            positions.add(Pair(x, y + textHeight / 2))
        }
        return positions
    }

    private fun getTextBounds(text: String, paint: Paint): Rect {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun getTimeInMillis(): Long {
        if (paused) {
            return pausedTime
        }

        return System.currentTimeMillis()
    }

    private fun Int.percent(percent: Int): Int {
        return (this.toFloat() / 100 * percent).toInt()
    }

    companion object {
        const val TEXT_RATIO = 0.111f
        const val DEGREES_IN_CIRCLE = 360
        const val DEGREES_START_OFFSET = 90
        const val PARTS_OF_CLOCK_AMOUNT = 12

        const val DESIRED_WIDTH = 500
        const val DESIRED_HEIGHT = 500

        const val SECOND_HAND_OFFSET_RATIO = 0.055f
        const val SECOND_HAND_WIDTH_RATIO = 0.480f
        const val SECOND_HAND_HEIGHT_RATIO = 0.015f

        const val MINUTE_HAND_OFFSET_RATIO = 0.026f
        const val MINUTE_HAND_WIDTH_RATIO = 0.416f
        const val MINUTE_HAND_HEIGHT_RATIO = 0.052f

        const val HOUR_HAND_OFFSET_RATIO = 0.026f
        const val HOUR_HAND_WIDTH_RATIO = 0.255f
        const val HOUR_HAND_HEIGHT_RATIO = 0.052f
    }
}
