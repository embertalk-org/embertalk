package edu.kit.tm.ps.embertalk.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import edu.kit.tm.ps.embertalk.model.contacts.ContactDao
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageDao
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageDao

@Database(
    entities = [Contact::class, Message::class, EncryptedMessage::class],
    version = 3,
    exportSchema = true
)
abstract class EmberDb : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    abstract fun messageDao(): MessageDao

    abstract fun encryptedMessageDao(): EncryptedMessageDao

    companion object {
        @Volatile
        private var Instance: EmberDb? = null

        fun getDb(context: Context): EmberDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, EmberDb::class.java, "message_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}