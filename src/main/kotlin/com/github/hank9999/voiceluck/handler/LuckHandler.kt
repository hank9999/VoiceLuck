package com.github.hank9999.voiceluck.handler

import com.github.hank9999.kook.card.Card
import com.github.hank9999.kook.card.CardMessage
import com.github.hank9999.kook.card.Element
import com.github.hank9999.kook.card.Module
import com.github.hank9999.kook.types.Type
import com.github.hank9999.kook.utils.NamedThreadFactory
import com.github.hank9999.voiceluck.VoiceLuck.db
import com.github.hank9999.voiceluck.VoiceLuck.kookApi
import com.github.hank9999.voiceluck.database.types.Luck
import com.github.hank9999.voiceluck.utils.Utils.Companion.createEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.concurrent.Executors

class LuckHandler {
    companion object {
        private val luckList: MutableList<Luck> = mutableListOf()
        val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor(NamedThreadFactory("LuckHandler")).asCoroutineDispatcher())
        private val logger: Logger = LoggerFactory.getLogger(LuckHandler::class.java)

        fun addLuckItem(item: Luck) {
            if (luckList.find { it.uuid == item.uuid } == null) {
                luckList.add(item)
            }
        }

        suspend fun newLuck(item: Luck) {
            if (item.time - System.currentTimeMillis() <= 60 * 1000) {
                addLuckItem(item)
            }
            db.lucks.insertOne(item)
        }

        suspend fun removeLuck(uuid: String) {
            db.lucks.findOneAndUpdate(Luck::uuid eq uuid, setValue(Luck::isOpen, true))
            luckList.removeIf { it.uuid == uuid }
        }

        suspend fun delLuck(uuid: String, guild: String): Boolean {
            try {
                val data = db.lucks.findOne(Luck::uuid eq uuid) ?: return false
                if (data.guild != guild) {
                    return false
                }
                try {
                    kookApi.Message().delete(data.luckMessageId)
                } catch (_: Exception) {}
                val dbResult = db.lucks.deleteOne((Luck::uuid eq uuid)).wasAcknowledged()
                luckList.removeIf { it.uuid == uuid }
                return dbResult
            } catch (_: Exception) {
                return false
            }
        }

        fun init() {
            coroutineScope.launch {
                while (true) {
                    try {
                        checkLuckList()
                    } catch (_: Exception) {}
                    delay(1000)
                }
            }
        }

        private suspend fun checkLuckList() {
            for (item in luckList.toList()) {
                if (System.currentTimeMillis() - item.time >= -100) {
                    logger.info("${item.uuid} 正在出奖")
                    removeLuck(item.uuid)
                    val userList = kookApi.Channel().userList(item.voiceChannel).toMutableList()
                    if (userList.size <= item.count) {
                        if (userList.size == 0) {
                            val card = Card(
                                theme = Type.Theme.SUCCESS,
                                Module.Header(Element.Text("语音抽奖: ${item.item}")),
                                Module.Divider(),
                                Module.Section(Element.Text("语音频道人数为0, 无人获奖", type = Type.Text.KMD)),
                                Module.Divider(),
                                Module.Context(Element.Text("抽奖ID: ${item.uuid}"))
                            )
                            logger.info("${item.uuid} 无人获奖")
                            kookApi.Message().createEx(item.channel, CardMessage(card), quote = item.luckMessageId)
                            continue
                        }
                        var userIdText = ""
                        userList.forEach {
                            userIdText += "(met)${it.id}(met) "
                        }
                        val card = Card(
                            theme = Type.Theme.SUCCESS,
                            Module.Header(Element.Text("语音抽奖: ${item.item}")),
                            Module.Divider(),
                            Module.Section(Element.Text("恭喜 $userIdText 获奖", type = Type.Text.KMD)),
                            Module.Divider(),
                            Module.Context(Element.Text("抽奖ID: ${item.uuid}"))
                        )
                        logger.info("${item.uuid} $userIdText 获奖")
                        kookApi.Message().createEx(item.channel, CardMessage(card), quote = item.luckMessageId)
                        continue
                    } else {
                        val secureRandom = SecureRandom()
                        val luckUserList: MutableList<String> = mutableListOf()
                        for (i in 1..item.count) {
                            val luckIndex = secureRandom.nextInt(userList.size)
                            luckUserList.add(userList.removeAt(luckIndex).id)
                        }
                        var userIdText = ""
                        luckUserList.forEach {
                            userIdText += "(met)$it(met) "
                        }
                        val card = Card(
                            theme = Type.Theme.SUCCESS,
                            Module.Header(Element.Text("语音抽奖: ${item.item}")),
                            Module.Divider(),
                            Module.Section(Element.Text("恭喜 $userIdText 获奖", type = Type.Text.KMD)),
                            Module.Divider(),
                            Module.Context(Element.Text("抽奖ID: ${item.uuid}"))
                        )
                        logger.info("${item.uuid} $userIdText 获奖")
                        kookApi.Message().createEx(item.channel, CardMessage(card), quote = item.luckMessageId)
                        continue
                    }
                }
            }
        }
    }
}