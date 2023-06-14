package edu.kit.tm.ps.embertalk.app

import android.content.Context
import edu.kit.tm.ps.embertalk.storage.MessageManager
import edu.kit.tm.ps.embertalk.storage.decrypted.MessageDb
import edu.kit.tm.ps.embertalk.storage.decrypted.OfflineMessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessageDb
import edu.kit.tm.ps.embertalk.storage.encrypted.OfflineEncryptedMessageRepository

interface AppContainer {
    val messageManager: MessageManager
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val messageManager: MessageManager by lazy {
        MessageManager(
            OfflineMessageRepository(MessageDb.getDb(context).dao()),
            OfflineEncryptedMessageRepository(EncryptedMessageDb.getDb(context).dao())
        )
    }
}