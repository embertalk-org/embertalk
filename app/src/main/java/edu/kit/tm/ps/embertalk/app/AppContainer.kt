package edu.kit.tm.ps.embertalk.app

import android.content.Context
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.epoch.MajorityVoteOffsetProvider
import edu.kit.tm.ps.embertalk.epoch.SysTimeEpochprovider
import edu.kit.tm.ps.embertalk.model.contacts.ContactDb
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import edu.kit.tm.ps.embertalk.model.contacts.OfflineContactRepository
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageDb
import edu.kit.tm.ps.embertalk.model.messages.decrypted.OfflineMessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageDb
import edu.kit.tm.ps.embertalk.model.messages.encrypted.OfflineEncryptedMessageRepository
import edu.kit.tm.ps.embertalk.sync.Synchronizer

interface AppContainer {
    val messageManager: MessageManager
    val contactManager: ContactManager
    val synchronizer: Synchronizer
    val cryptoService: CryptoService
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val majorityVoteOffsetProvider: MajorityVoteOffsetProvider = MajorityVoteOffsetProvider(SysTimeEpochprovider())

    override val messageManager: MessageManager by lazy {
        MessageManager(
            OfflineMessageRepository(MessageDb.getDb(context).dao()),
            OfflineEncryptedMessageRepository(EncryptedMessageDb.getDb(context).dao()),
            cryptoService
        )
    }

    override val contactManager: ContactManager by lazy {
        ContactManager(
            OfflineContactRepository(ContactDb.getDb(context).dao())
        )
    }

    override val synchronizer: Synchronizer by lazy {
        Synchronizer(messageManager, majorityVoteOffsetProvider, majorityVoteOffsetProvider)
    }

    override val cryptoService: CryptoService by lazy {
        CryptoService(majorityVoteOffsetProvider, prefs)
    }
}