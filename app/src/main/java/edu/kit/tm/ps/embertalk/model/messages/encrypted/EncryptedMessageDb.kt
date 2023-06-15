package edu.kit.tm.ps.embertalk.model.messages.encrypted

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EncryptedMessage::class], version = 1, exportSchema = false)
abstract class EncryptedMessageDb : RoomDatabase() {
    abstract fun dao(): EncryptedMessageDao

    companion object {
        @Volatile
        private var Instance: EncryptedMessageDb? = null

        fun getDb(context: Context): EncryptedMessageDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, EncryptedMessageDb::class.java, "encrypted_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}