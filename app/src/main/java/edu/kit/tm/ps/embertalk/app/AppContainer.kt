package edu.kit.tm.ps.embertalk.app

import android.content.Context
import edu.kit.tm.ps.embertalk.storage.MessageDb
import edu.kit.tm.ps.embertalk.storage.MessageRepository
import edu.kit.tm.ps.embertalk.storage.OfflineMessageRepository

interface AppContainer {
    val messageRepository: MessageRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val messageRepository: MessageRepository by lazy {
        OfflineMessageRepository(MessageDb.getDb(context).messageDao())
    }
}