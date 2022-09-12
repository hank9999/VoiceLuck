package com.github.hank9999.voiceluck.types

import kotlinx.serialization.Serializable

@Serializable
data class ConfigFile(
    val token: String,
    val verifyToken: String = "",
    val cmdPrefix: List<String> = listOf(".", "。", "/"),
    val host: String = "",
    val path: String = "",
    val port: Int = 0,
    val useWebhook: Boolean = false,
    val loggerLevel: LoggerLevel = LoggerLevel(),
    val database: Database = Database(),
    val superAdmin: SuperAdmin = SuperAdmin()
) {
    @Serializable
    data class LoggerLevel(
        val main: String = "INFO",
        val jetty: String = "INFO",
        val mongodb: String = "WARN"
    )

    @Serializable
    data class Database(
        val host: String = "",
        val port: Int = 0,
        val user: String = "",
        val password: String = "",
        val database: String = "VoiceLuck"
    )

    @Serializable
    data class SuperAdmin(
        val users: Set<String> = setOf(),
        val guild: Map<String, Set<String>> = mapOf()
    )
}