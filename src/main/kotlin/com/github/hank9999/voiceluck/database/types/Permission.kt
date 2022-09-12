package com.github.hank9999.voiceluck.database.types

data class Permission(
    val guild: String,
    val roles: MutableSet<Int> = mutableSetOf(),
)