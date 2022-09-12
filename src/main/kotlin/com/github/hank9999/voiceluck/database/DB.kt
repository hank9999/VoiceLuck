package com.github.hank9999.voiceluck.database

import com.github.hank9999.voiceluck.database.types.Luck
import com.github.hank9999.voiceluck.database.types.Permission
import com.github.hank9999.voiceluck.types.ConfigFile
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*

class DB(config: ConfigFile) {
    private val conn  = if (config.database.user.isEmpty() || config.database.password.isEmpty()) {
        "mongodb://${config.database.host}:${config.database.port}"
    } else {
        "mongodb://${config.database.user}:${config.database.password}@${config.database.host}:${config.database.port}"
    }
    private val client = KMongo.createClient(conn).coroutine
    private val db = client.getDatabase(config.database.database)
    val tokens = db.getCollection<Permission>()
    val lucks = db.getCollection<Luck>()
}