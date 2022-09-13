package com.github.hank9999.voiceluck

import com.github.hank9999.kook.Bot
import com.github.hank9999.kook.card.Card
import com.github.hank9999.kook.card.CardMessage
import com.github.hank9999.kook.card.Element
import com.github.hank9999.kook.card.Module
import com.github.hank9999.kook.json.JSON
import com.github.hank9999.kook.json.JSON.Extension.Int
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.types.Event
import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.Type
import com.github.hank9999.kook.types.User
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.voiceluck.VoiceLuck.kookApi
import com.github.hank9999.voiceluck.database.types.Luck
import com.github.hank9999.voiceluck.handler.LuckHandler
import com.github.hank9999.voiceluck.permission.PMAdd
import com.github.hank9999.voiceluck.permission.PMCheck
import com.github.hank9999.voiceluck.permission.PMDel
import com.github.hank9999.voiceluck.utils.Generators.Companion.genPermissionCard
import com.github.hank9999.voiceluck.utils.TimeUtils
import com.github.hank9999.voiceluck.utils.Utils.Companion.replyEx
import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern

class Commands {
    private val logger: Logger = LoggerFactory.getLogger(Commands::class.java)
    val needRemoveEscapePatten = Pattern.compile("(?i)\\\\*~\\[]\\(\\)->:`")

    @Bot.OnCommand("ping")
    suspend fun hello(msg: Message) {
        val netDelay = System.currentTimeMillis() - msg.msgTimestamp
        msg.replyEx("Pong! 机器人正常运行中~\n服务器ID: ${msg.extra.guildId}\n频道ID: ${msg.targetId}\n用户ID: ${msg.authorId}\n延迟: $netDelay ms")
    }

    // 抽奖指令格式: 语音抽奖 [时间] [数量] [名称]
    // 中间使用空格间隔
    @Bot.OnCommand("voiceluck", aliases = ["语音抽奖"])
    suspend fun luck(msg: Message) {
        if (!PMCheck.checkLuck(msg.extra.guildId, msg.authorId, msg.extra.author.roles)) {
            return
        }
        val params = getParams(msg.content.removeEscape())
        if (params.size < 3) {
            msg.replyEx("参数不正确")
            return
        }
        val timeWait = TimeUtils.timeHandle(params.removeAt(0))
        if (timeWait == 0) {
            msg.replyEx("时间设置不正确, 请检查后重试")
            return
        }
        var count = 0
        try {
            count = params.removeAt(0).toInt()
        } catch (_: Exception) {}
        if (count == 0) {
            msg.replyEx("数量设置不正确, 请检查后重试")
            return
        }
        val item = params.joinToString(" ")
        val voiceChannelList = kookApi.ChannelUser().getJoinedChannel(msg.extra.guildId, msg.authorId)
        if (voiceChannelList.isEmpty()) {
            msg.replyEx("获取语音频道失败, 请重新进入语音频道后重试")
            return
        }
        val endTime = System.currentTimeMillis() + timeWait * 1000
        val voiceChannel = voiceChannelList.first()
        val uuid = UUID.randomUUID().toString()
        val card = Card(
            theme = Type.Theme.SUCCESS,
            Module.Header(Element.Text("语音抽奖: $item")),
            Module.Divider(),
            Module.Section(Element.Text("发起人: (met)${msg.authorId}(met)", type = Type.Text.KMD)),
            Module.Section(Element.Text("名额: $count")),
            Module.Section(Element.Text("抽奖语音频道: ${voiceChannel.name}")),
            Module.Countdown(Type.CountdownMode.DAY, endTime),
            Module.Divider(),
            Module.Context(Element.Text("抽奖ID: $uuid"))
        )
        val msgId = msg.replyEx(CardMessage(card)).msgId
        val luck = Luck(
            uuid,
            endTime,
            count,
            item,
            msg.extra.guildId,
            msg.targetId,
            voiceChannel.id,
            msg.msgId,
            msgId
        )
        LuckHandler.newLuck(luck)
        logger.info("新增抽奖 $luck")
    }

    @Bot.OnCommand("permission", aliases = ["权限管理"])
    suspend fun permission(msg: Message) {
        if (!PMCheck.checkAdmin(msg.extra.guildId, msg.authorId)) {
            msg.replyEx("您没有权限执行此操作")
            return
        }
        val card = genPermissionCard(msg.extra.guildId)
        msg.replyEx(card)
    }

    @Bot.OnEvent(EventTypes.MESSAGE_BTN_CLICK)
    suspend fun buttonClickHandler(event: Event) {
        try {
            val user = JSON.json.decodeFromJsonElement<User>(event.extra.body["user_info"])
            val guildId = event.extra.body["guild_id"].String
            val channelId = event.extra.body["target_id"].String
            if (!PMCheck.checkAdmin(guildId, user.id)) {
                kookApi.Message().create(channelId, "您没有权限进行此操作", tempTargetId = user.id)
                return
            }
            val value = JSON.json.parseToJsonElement(event.extra.body["value"].String)
            val msgId = event.extra.body["msg_id"].String
            when (value["type"].String) {
                "closePanel" -> kookApi.Message().delete(msgId)
                "permRemove" -> {
                    val roleId = value["role"].Int
                    if (!PMCheck.checkRole(guildId, roleId)) {
                        kookApi.Message().create(channelId, "角色 $roleId 已没有语音抽奖权限", tempTargetId = user.id)
                    } else {
                        if (PMDel.delRoles(guildId, listOf(roleId))) {
                            kookApi.Message().update(msgId, genPermissionCard(guildId))
                        } else {
                            kookApi.Message().create(channelId, "语音抽奖权限更新失败, 可能从未添加过权限, 请先添加一个或稍后再试, 如还无法使用 请联系维护", tempTargetId = user.id)
                        }
                    }
                }
                "permGive" -> {
                    val roleId = value["role"].Int
                    if (PMCheck.checkRole(guildId, roleId)) {
                        kookApi.Message().create(channelId, "角色 $roleId 已有语音抽奖权限", tempTargetId = user.id)
                    } else {
                        if (PMAdd.addRoles(guildId, listOf(roleId))) {
                            kookApi.Message().update(msgId, genPermissionCard(guildId))
                        } else {
                            kookApi.Message().create(channelId, "语音抽奖权限更新失败, 请稍后再试或联系维护", tempTargetId = user.id)
                        }
                    }
                }
                else -> {}
            }
        } catch (ex: Exception) {
            logger.error("${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    fun getParams(content: String): MutableList<String> {
        val params = content.split(" ").toMutableList()
        params.removeAt(0)
        return params
    }

    private fun String.removeEscape(): String {
        return needRemoveEscapePatten.matcher(this).replaceAll("")
    }
}