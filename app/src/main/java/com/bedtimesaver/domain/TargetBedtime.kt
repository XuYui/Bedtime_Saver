package com.bedtimesaver.domain

import java.time.LocalTime

data class TargetBedtime(
    val hour: Int = 23,
    val minute: Int = 0,
) {
    fun asMinutesOfDay(): Int = hour * 60 + minute

    fun asLocalTime(): LocalTime = LocalTime.of(hour, minute)

    fun displayText(): String = "%02d:%02d".format(hour, minute)

    fun withHourDelta(delta: Int): TargetBedtime {
        val nextHour = (hour + delta).floorMod(24)
        return copy(hour = nextHour)
    }

    fun withMinuteDelta(delta: Int): TargetBedtime {
        val total = (asMinutesOfDay() + delta).floorMod(24 * 60)
        return TargetBedtime(hour = total / 60, minute = total % 60)
    }
}

private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus
