package edu.kit.tm.ps.embertalk.app

import android.content.Context
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.crypto.Keys
import edu.kit.tm.ps.embertalk.model.contacts.ContactDb
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import edu.kit.tm.ps.embertalk.model.contacts.OfflineContactRepository
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageDb
import edu.kit.tm.ps.embertalk.model.messages.decrypted.OfflineMessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageDb
import edu.kit.tm.ps.embertalk.model.messages.encrypted.OfflineEncryptedMessageRepository

interface AppContainer {
    val messageManager: MessageManager
    val contactManager: ContactManager
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val messageManager: MessageManager by lazy {
        MessageManager(
            OfflineMessageRepository(MessageDb.getDb(context).dao()),
            OfflineEncryptedMessageRepository(EncryptedMessageDb.getDb(context).dao()),
            Keys(PreferenceManager.getDefaultSharedPreferences(context))
        )
    }

    override val contactManager: ContactManager by lazy {
        ContactManager(
            OfflineContactRepository(ContactDb.getDb(context).dao())
        )
    }
}