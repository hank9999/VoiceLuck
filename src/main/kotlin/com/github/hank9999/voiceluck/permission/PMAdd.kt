package com.github.hank9999.voiceluck.permission

import com.github.hank9999.voiceluck.VoiceLuck.db
import com.github.hank9999.voiceluck.database.types.Permission
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class PMAdd {
    companion object {
        suspend fun addRoles(guild: String, roles: List<Int>): Boolean {
            val result = db.tokens.findOne(Permission::guild eq guild)
            val permission = result ?: Permission(guild)
            permission.roles.addAll(roles.toSet())
            return if (result == null) {
                db.tokens.insertOne(permission).wasAcknowledged()
            } else {
                db.tokens.updateOne(Permission::guild eq guild, setValue(Permission::roles, permission.roles)).wasAcknowledged()
            }
        }
    }
}