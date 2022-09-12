package com.github.hank9999.voiceluck.utils

import java.util.regex.Pattern

class TimeUtils {
    companion object {
        private val timePattern: Pattern = Pattern.compile("((?:\\d+)[smhd])")
        private val timeKeyword = mapOf("s" to 1, "m" to 60, "h" to 3600, "d" to 86400)

        fun timeHandle(timeStr: String): Int {
            val m = timePattern.matcher(timeStr)
            var time = 0
            while (m.find()) {
                val str = m.group()
                val keyword = str.last().lowercase()
                val timeMagnification = timeKeyword[keyword] ?: return 0
                try {
                    val num = str.dropLast(1).toInt()
                    time += num * timeMagnification
                } catch (_: Exception) {}
            }
            return time
        }
    }
}