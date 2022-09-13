package com.github.hank9999.voiceluck.task

import com.github.hank9999.kook.utils.NamedThreadFactory
import com.github.hank9999.voiceluck.VoiceLuck.db
import com.github.hank9999.voiceluck.database.types.Luck
import com.github.hank9999.voiceluck.handler.LuckHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors

class LuckTask : TimerTask() {
    override fun run() {
        coroutineScope.launch {
            try {
                db.lucks.find(Luck::isOpen eq false).toFlow().collect {
                    if (it.time - System.currentTimeMillis() <= 60 * 1000) {
                        LuckHandler.addLuckItem(it)
                    }
                }
            } catch (_: Exception) {}
        }
    }
    companion object {
        val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor(NamedThreadFactory("LuckTask")).asCoroutineDispatcher())
        private val logger: Logger = LoggerFactory.getLogger(LuckTask::class.java)
    }
}