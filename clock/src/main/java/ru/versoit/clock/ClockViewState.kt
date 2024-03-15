package ru.versoit.clock

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.DrawableRes

class ClockViewState : View.BaseSavedState {

    val secondHandColor: Int
    val minuteHandColor: Int
    val hourHandColor: Int
    val textColor: Int
    val colorContainer: Int
    @DrawableRes
    val clockShape: Int
    val paused: Boolean
    val pausedTime: Long

    constructor(
        parcel: Parcel,
    ) : super(parcel) {
        this.secondHandColor = parcel.readInt()
        this.minuteHandColor = parcel.readInt()
        this.hourHandColor = parcel.readInt()
        this.textColor = parcel.readInt()
        this.colorContainer = parcel.readInt()
        this.clockShape = parcel.readInt()
        this.paused = parcel.readByte().toInt() != 0
        this.pausedTime = parcel.readLong()
    }

    constructor(
        superState: Parcelable?,
        textColor: Int = 0,
        colorContainer: Int = 0,
        secondHandColor: Int = 0,
        minuteHandColor: Int = 0,
        hourHandColor: Int = 0,
        paused: Boolean = false,
        pausedTime: Long = 0L,
        @DrawableRes clockShape: Int = R.drawable.clock_shape,
    ) : super(
        superState
    ) {
        this.secondHandColor = secondHandColor
        this.minuteHandColor = minuteHandColor
        this.hourHandColor = hourHandColor
        this.textColor = textColor
        this.colorContainer = colorContainer
        this.clockShape = clockShape
        this.paused = paused
        this.pausedTime = pausedTime
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeInt(secondHandColor)
        parcel.writeInt(minuteHandColor)
        parcel.writeInt(hourHandColor)
        parcel.writeInt(textColor)
        parcel.writeInt(colorContainer)
        parcel.writeInt(clockShape)
        parcel.writeByte(if (paused) 1 else 0)
        parcel.writeLong(pausedTime)
    }

    companion object CREATOR : Parcelable.Creator<ClockViewState> {
        override fun createFromParcel(parcel: Parcel): ClockViewState {
            return ClockViewState(parcel)
        }

        override fun newArray(size: Int): Array<ClockViewState?> {
            return arrayOfNulls(size)
        }
    }

}