package com.github.hank9999.voiceluck.permission

import com.github.hank9999.voiceluck.VoiceLuck.config
import com.github.hank9999.voiceluck.VoiceLuck.db
import com.github.hank9999.voiceluck.VoiceLuck.kookApi
import com.github.hank9999.voiceluck.database.types.Permission
import org.litote.kmongo.eq

class PMCheck {
    data class GuildMasterCacheData(
        val guild: String,
        val master: String,
        val time: Long
    )

    companion object {
        private val guildMasterCache: MutableList<GuildMasterCacheData> = mutableListOf()

        fun removeCache(item: GuildMasterCacheData) {
            guildMasterCache.remove(item)
        }

        fun getCache(): List<GuildMasterCacheData> {
            return guildMasterCache.toList()
        }

        suspend fun checkGuildMaster(guild: String, userId: String): Boolean {
            var cacheData = guildMasterCache.find { it.guild == guild }
            if (cacheData != null) {
                if (System.currentTimeMillis() - cacheData.time >= 10 * 60 * 1000) {
                    guildMasterCache.remove(cacheData)
                    cacheData = null
                }
            }
            val guildMasterId: String = cacheData?.master ?: kookApi.Guild().view(guild).masterId
            if (cacheData == null) {
                guildMasterCache.add(GuildMasterCacheData(guild, guildMasterId, System.currentTimeMillis()))
            }
            return guildMasterId == userId
        }

        suspend fun checkSuperAdmin(guild: String, userId: String): Boolean {
            return if (config.superAdmin.users.contains(userId)) {
                true
            } else {
                config.superAdmin.guild.containsKey(guild) && config.superAdmin.guild[guild]!!.contains(userId)
            }
        }

        suspend fun checkRoles(guild: String, roles: List<Int>): Boolean {
            val permission = db.tokens.findOne(Permission::guild eq guild) ?: return false
            return (permission.roles intersect roles.toSet()).isNotEmpty()
        }

        suspend fun checkLuck(guild: String, userId: String, roles: List<Int>): Boolean {
            return if (checkGuildMaster(guild, userId)) {
                true
            } else if (checkSuperAdmin(guild, userId)) {
                true
            } else {
                checkRoles(guild, roles)
            }
        }

        suspend fun checkAdmin(guild: String, userId: String): Boolean {
            return if (checkGuildMaster(guild, userId)) {
                true
            } else {
                checkSuperAdmin(guild, userId)
            }
        }
    }
}