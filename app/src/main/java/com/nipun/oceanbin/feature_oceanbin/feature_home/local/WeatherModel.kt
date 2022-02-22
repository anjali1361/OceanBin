package com.nipun.oceanbin.feature_oceanbin.feature_home.local

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

data class WeatherModel(
    val location: String = "Bariatu, Ranchi",
    val temperature: Int = 25,
    val weather: String = "Shiny Day",
) {

    fun getCurrentDate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            val formatTime = currentTime.format(formatter).substring(0, 12)
            LocalDateTime.now().dayOfWeek.name + ", " + if (formatTime.get(formatTime.lastIndex) == ',') formatTime.substring(
                0,
                formatTime.lastIndex
            ) else formatTime
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
    }
}
