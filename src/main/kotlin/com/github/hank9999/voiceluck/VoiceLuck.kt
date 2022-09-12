package com.github.hank9999.voiceluck

import com.github.hank9999.kook.Bot
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.http.KookApi
import com.github.hank9999.voiceluck.database.DB
import com.github.hank9999.voiceluck.handler.LuckHandler
import com.github.hank9999.voiceluck.task.CacheTask
import com.github.hank9999.voiceluck.task.LuckTask
import com.github.hank9999.voiceluck.types.ConfigFile
import com.github.hank9999.voiceluck.utils.LoggerLevels
import io.javalin.core.util.JavalinLogger
import io.javalin.jetty.JettyUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.fusesource.jansi.AnsiConsole
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.system.exitProcess

object VoiceLuck {
    private val logger: Logger = LoggerFactory.getLogger(VoiceLuck::class.java)
    lateinit var kookApi: KookApi
    lateinit var httpApi: HttpApi
    lateinit var db: DB
    lateinit var config: ConfigFile

    @JvmStatic
    fun main(args: Array<String>) {

        // 初始化彩色日志
        AnsiConsole.systemInstall()

        logger.info(
            """
            |
            |     _    __          _                 __                     __  
            |    | |  / / ____    (_) _____  ___    / /     __  __  _____  / /__
            |    | | / / / __ \  / / / ___/ / _ \  / /     / / / / / ___/ / //_/
            |    | |/ / / /_/ / / / / /__  /  __/ / /___  / /_/ / / /__  / ,<   
            |    |___/  \____/ /_/  \___/  \___/ /_____/  \__,_/  \___/ /_/|_|  
            |
            """.trimMargin()
        )

        // 配置文件检查和读取
        if (!Config.checkExists()) {
            logger.error("未找到配置文件")
            logger.info("已生成配置文件，请配置后再启动程序")
            exitProcess(1)
        }
        config = Config.readConfig()

        // 设置日志等级
        Configurator.setAllLevels(LogManager.getRootLogger().name, LoggerLevels.fromString(config.loggerLevel.main))
        Configurator.setAllLevels("org.eclipse.jetty", LoggerLevels.fromString(config.loggerLevel.jetty))
        Configurator.setAllLevels("org.mongodb.driver", LoggerLevels.fromString(config.loggerLevel.mongodb))

        // 禁用 Jetty 和 Javalin 启动初始化日志
        JettyUtil.logDuringStartup = false
        JavalinLogger.startupInfo = false

        // 初始化数据库
        db = DB(config)

        // 初始化 KOOK SDK
        val bot = if (config.useWebhook) {
            Bot(com.github.hank9999.kook.Config(
                token = config.token,
                cmd_prefix = config.cmdPrefix,
                verify_token = config.verifyToken,
                host = config.host,
                port = config.port,
                path = config.path
            ))
        } else {
            Bot(com.github.hank9999.kook.Config(
                token = config.token,
                cmd_prefix = config.cmdPrefix
            ))
        }

        // 初始化 api 变量
        httpApi = bot.httpApi
        kookApi = bot.kookApi

        // 注册机器人指令
        bot.registerClass(Commands())

        // 每 5 分钟检查一次 Cache, 清除过期缓存
        Timer().schedule(CacheTask(), Date(), 5 * 60 * 1000)

        // 每 1 分钟检查一次 数据库, 获取即将开奖的抽奖
        Timer().schedule(LuckTask(), Date(), 60 * 1000)

        // 初始化抽奖处理器
        LuckHandler.init()
    }
}