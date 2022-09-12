package com.github.hank9999.voiceluck.database.types

data class Luck(
    val uuid: String,
    val time: Long,
    val count: Int,
    val item: String,
    val guild: String,
    val channel: String,
    val voiceChannel: String,
    val commandMessageId: String,
    val luckMessageId: String,
    var isOpen: Boolean = false
)