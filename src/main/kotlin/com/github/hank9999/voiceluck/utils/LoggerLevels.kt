package com.github.hank9999.voiceluck.utils

import org.apache.logging.log4j.Level

enum class LoggerLevels(val str: String, val level: Level) {
    ALL("ALL", Level.ALL),
    TRACE("TRACE", Level.TRACE),
    DEBUG("DEBUG", Level.DEBUG),
    INFO("INFO", Level.INFO),
    WARN("WARN", Level.WARN),
    ERROR("ERROR", Level.ERROR),
    FATAL("FATAL", Level.FATAL),
    OFF("OFF", Level.OFF);

    companion object {
        fun fromString(str: String): Level {
            return (LoggerLevels.values().find { it.str.equals(str, ignoreCase = true) } ?: INFO).level
        }
    }
}