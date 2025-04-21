package edu.kit.tm.ps.embertalk.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.epoch.SysTimeEpochprovider
import edu.kit.tm.ps.embertalk.model.EmberDb
import edu.kit.tm.ps.embertalk.model.contacts.ContactDao
import edu.kit.tm.ps.embertalk.model.contacts.ContactRepository
import edu.kit.tm.ps.embertalk.model.contacts.OfflineContactRepository
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageDao
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.model.messages.decrypted.OfflineMessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageDao
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.OfflineEncryptedMessageRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun contactRepository(contactDao: ContactDao): ContactRepository {
        return OfflineContactRepository(contactDao)
    }

    @Provides
    fun messageRepository(messageDao: MessageDao): MessageRepository {
        return OfflineMessageRepository(messageDao)
    }

    @Provides
    fun encryptedRepository(encryptedMessageDao: EncryptedMessageDao): EncryptedMessageRepository {
        return OfflineEncryptedMessageRepository(encryptedMessageDao)
    }

    @Provides
    fun walletDb(@ApplicationContext context: Context): EmberDb {
        return EmberDb.getDb(context)
    }

    @Provides
    fun contactDao(emberDb: EmberDb): ContactDao {
        return emberDb.contactDao()
    }

    @Provides
    fun messageDao(emberDb: EmberDb): MessageDao {
        return emberDb.messageDao()
    }

    @Provides
    fun encryptedDao(emberDb: EmberDb): EncryptedMessageDao {
        return emberDb.encryptedMessageDao()
    }

    @Provides
    fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    fun epochProvider(): EpochProvider {
        return SysTimeEpochprovider()
    }
}
