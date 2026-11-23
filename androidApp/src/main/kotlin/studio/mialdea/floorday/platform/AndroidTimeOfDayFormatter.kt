package studio.mialdea.floorday.platform

import android.content.Context
import android.text.format.DateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.platform.TimeOfDayFormatter

/** Follows the system 12/24 h switch and the locale's order ("08:00", "8:00 AM"). */
class AndroidTimeOfDayFormatter(private val context: Context) : TimeOfDayFormatter {
    override fun format(time: LocalTime): String {
        val locale = Locale.getDefault()
        val skeleton = if (DateFormat.is24HourFormat(context)) "Hm" else "hma"
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
        return java.time.LocalTime.of(time.hour, time.minute).format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}
