package com.github.hank9999.voiceluck.permission

import com.github.hank9999.voiceluck.VoiceLuck.db
import com.github.hank9999.voiceluck.database.types.Permission
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class PMDel {
    suspend fun delRoles(guild: String, roles: List<Int>) {
        val permission = db.tokens.findOne(Permission::guild eq guild) ?: return
        permission.roles.removeAll(roles.toSet())
        db.tokens.updateOne(Permission::guild eq guild, setValue(Permission::roles, permission.roles))
    }
}