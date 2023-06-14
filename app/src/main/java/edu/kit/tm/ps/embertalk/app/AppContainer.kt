package edu.kit.tm.ps.embertalk.app

import android.content.Context
import edu.kit.tm.ps.embertalk.storage.decoded.DecodedMessageDb
import edu.kit.tm.ps.embertalk.storage.decoded.DecodedMessageRepository
import edu.kit.tm.ps.embertalk.storage.decoded.OfflineDecodedMessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.MessageDb
import edu.kit.tm.ps.embertalk.storage.encrypted.MessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.OfflineMessageRepository

interface AppContainer {
    val messageRepository: MessageRepository
    val decodedMessageRepository: DecodedMessageRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val messageRepository: MessageRepository by lazy {
        OfflineMessageRepository(MessageDb.getDb(context).dao())
    }

    override val decodedMessageRepository: DecodedMessageRepository by lazy {
        OfflineDecodedMessageRepository(DecodedMessageDb.getDb(context).dao())
    }
}