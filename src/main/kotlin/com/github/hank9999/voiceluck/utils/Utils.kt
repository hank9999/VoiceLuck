package com.github.hank9999.voiceluck.utils

import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.MessageTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Utils {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Utils::class.java)
        suspend fun Message.replyEx(content: Any, type: MessageTypes? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
            return try {
                this.reply(content, type, nonce, tempTargetId)
            } catch (ex: Exception) {
                logger.error("${ex.message}\n${ex.stackTraceToString()}")
                try {
                    this.reply("发生错误, 请稍后再试或联系维护\n${ex.message}")
                } catch (_: Exception) {
                    MessageCreate()
                }
            }
        }

        suspend fun com.github.hank9999.kook.http.kookapis.Message.createEx(targetId: String, content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
            return try {
                this.create(targetId, content, type, quote, nonce, tempTargetId)
            } catch (ex: Exception) {
                logger.error("${ex.message}\n${ex.stackTraceToString()}")
                try {
                    this.create(targetId, "发生错误, 请稍后再试或联系维护\n${ex.message}")
                } catch (_: Exception) {
                    MessageCreate()
                }
            }
        }
    }
}