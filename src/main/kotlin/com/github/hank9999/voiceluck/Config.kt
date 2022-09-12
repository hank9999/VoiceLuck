package com.github.hank9999.voiceluck

import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.voiceluck.types.ConfigFile
import kotlinx.serialization.decodeFromString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Config {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Config::class.java)
        private const val configFile = "config.json"

        fun checkExists(): Boolean {
            val file = File(configFile)
            if (file.exists()) {
                return true
            } else {
                val inputStream: InputStream = Config::class.java.getResourceAsStream("/$configFile")!!
                try {
                    Files.copy(inputStream, Paths.get(configFile))
                } catch (e: FileNotFoundException) {
                    logger.error(e.stackTraceToString())
                    logger.error("配置文件错误: 复制配置文件时未找到程序内文件")
                    exitProcess(1)
                } catch (e: IOException) {
                    logger.error(e.stackTraceToString())
                    logger.error("配置文件错误: 复制配置文件时IO错误")
                    exitProcess(1)
                }
            }
            return false
        }

        fun readConfig(): ConfigFile {
            val file = File(configFile)
            val jsonText = file.readText()
            val config = json.decodeFromString<ConfigFile>(jsonText)
            if (config.token.isEmpty()) {
                logger.error("配置文件错误: token 不能为空")
                exitProcess(1)
            }
            if (config.useWebhook) {
                if (config.host.isEmpty() || config.port !in 0..65535 || config.path.isEmpty()) {
                    logger.error("配置文件错误: 已启用 WebHook 但 WebHook 配置不合规")
                    exitProcess(1)
                }
                if (config.verifyToken.isEmpty()) {
                    logger.warn("建议配置 verifyToken 来增加安全性")
                }
            }
            if (config.database.host.isEmpty() || config.database.port == 0 || config.database.database.isEmpty()) {
                logger.error("配置文件错误: 数据库配置不合规")
                exitProcess(1)
            }
            return config
        }
    }
}