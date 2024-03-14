package ru.versoit.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
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

    var textColor: Int
    var colorContainer: Int
    var secondHandColor: Int
    var minuteHandColor: Int
    var hourHandColor: Int

    var clockBackground: Drawable?

    private val minuteHandDrawable = AppCompatResources.getDrawable(context, R.drawable.minute_hand)
    private val hourHandDrawable = AppCompatResources.getDrawable(context, R.drawable.hour_hand)

    private var cachedMeasurements = Measurements()

    private val paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textAlignment = TEXT_ALIGNMENT_CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)

        textColor = typedArray.getColor(R.styleable.ClockView_android_textColor, Color.BLACK)
        colorContainer = typedArray.getColor(R.styleable.ClockView_colorContainer, Color.WHITE)
        clockBackground = AppCompatResources.getDrawable(
            context,
            typedArray.getResourceId(
                R.styleable.ClockView_android_background,
                R.drawable.clock_background
            )
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

        invalidate()
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
        paint.color = textColor
        for (index in 0..<PARTS_OF_CLOCK_AMOUNT) {
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
        paint.color = color
        drawHand(
            handDimensions.offsetRatio,
            handDimensions.widthRatio,
            handDimensions.heightRatio,
            time = time,
            millisInOneCircle = millisInOneCircle,
        ) { left, top, right, bottom, rotation ->
            canvas.save()
            canvas.rotate(rotation, cachedMeasurements.centerX, cachedMeasurements.centerY)
            drawable?.setTint(minuteHandColor)
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
        radius: Int,
    ): List<Pair<Float, Float>> {
        val positions = mutableListOf<Pair<Float, Float>>()
        val angleIncrement = 2 * Math.PI / PARTS_OF_CLOCK_AMOUNT
        for (index in 1..PARTS_OF_CLOCK_AMOUNT) {
            val number = index.toString()

            val angle = angleIncrement * index - Math.PI / 2
            val textHeight = getTextBounds(number).height()
            val x = centerX + (radius - textHeight) * cos(angle).toFloat()
            val y = centerY + (radius - textHeight) * sin(angle).toFloat()

            positions.add(Pair(x, y + textHeight / 2))
        }
        return positions
    }

    private fun getTextBounds(text: String): Rect {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun getTimeInMillis(): Long {
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
        const val HOUR_HAND_WIDTH_RATIO = 0.263f
        const val HOUR_HAND_HEIGHT_RATIO = 0.052f
    }
}
