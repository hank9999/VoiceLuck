package com.github.hank9999.voiceluck.utils

import com.github.hank9999.kook.card.Card
import com.github.hank9999.kook.card.CardMessage
import com.github.hank9999.kook.card.Element
import com.github.hank9999.kook.card.Module
import com.github.hank9999.kook.types.Type
import com.github.hank9999.voiceluck.VoiceLuck.kookApi
import com.github.hank9999.voiceluck.permission.PMCheck

class Generators {
    companion object {
        suspend fun genPermissionCard(guild: String): CardMessage {
            val cardMessage = CardMessage()
            var card = Card(
                theme = Type.Theme.SECONDARY,
                Module.Header(Element.Text("VoiceLuck 语音抽奖 | 权限管理")),
                Module.Divider()
            )
            val roleList = kookApi.GuildRole().list(guild)
            var clearUsed = false
            for (it in roleList) {
                val perm = PMCheck.checkRole(guild, it.roleId)
                val permText = when (perm) {
                    true -> "权限: **有**"
                    false -> "权限: **无**"
                }
                val role = if (it.roleId == 0) "@全体成员" else "(rol)${it.roleId}(rol)"
                val text = "> $role\n$permText\n\n"
                if (card.length() + 2 > 50) {
                    if (cardMessage.length() == 4) {
                        card.clear()
                        clearUsed = true
                    } else {
                        cardMessage.append(card)
                        card = Card()
                    }
                }
                if (perm) {
                    card.append(
                        Module.Section(
                            text = Element.Text(text, type = Type.Text.KMD),
                            mode = Type.SectionMode.RIGHT,
                            accessory = Element.Button(Type.Theme.DANGER, Element.Text("移除"), "{\"type\":\"permRemove\", \"role\": ${it.roleId}}", Type.Click.RETURN_VAL)
                        )
                    )
                } else {
                    card.append(
                        Module.Section(
                            text = Element.Text(text, type = Type.Text.KMD),
                            mode = Type.SectionMode.RIGHT,
                            accessory = Element.Button(Type.Theme.SUCCESS, Element.Text("给予"), "{\"type\":\"permGive\", \"role\": ${it.roleId}}", Type.Click.RETURN_VAL)
                        )
                    )
                }
            }
            if (card.length() >= 48 && cardMessage.length() < 4) {
                cardMessage.append(card)
                card = Card()
            }
            if (clearUsed) {
                if (card.length() == 50) {
                    card.removeAt(49)
                    card.removeAt(48)
                } else if (card.length() == 49) {
                    card.removeAt(48)
                }
                card.append(Module.Section(Element.Text("因卡片消息限制, 部分服务器信息无法发送")))
            }
            card.append(
                Module.ActionGroup(
                    Element.Button(Type.Theme.WARNING, Element.Text("关闭"), "{\"type\":\"closePanel\"}", Type.Click.RETURN_VAL)
                )
            )
            cardMessage.append(card)
            return cardMessage
        }
    }
}