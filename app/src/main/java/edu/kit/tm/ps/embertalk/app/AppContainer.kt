package edu.kit.tm.ps.embertalk.app

import android.content.Context
import edu.kit.tm.ps.embertalk.storage.decrypted.MessageDb
import edu.kit.tm.ps.embertalk.storage.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.storage.decrypted.OfflineMessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessageDb
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.OfflineEncryptedMessageRepository

interface AppContainer {
    val encryptedMessageRepository: EncryptedMessageRepository
    val messageRepository: MessageRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val encryptedMessageRepository: EncryptedMessageRepository by lazy {
        OfflineEncryptedMessageRepository(EncryptedMessageDb.getDb(context).dao())
    }

    override val messageRepository: MessageRepository by lazy {
        OfflineMessageRepository(MessageDb.getDb(context).dao())
    }
}