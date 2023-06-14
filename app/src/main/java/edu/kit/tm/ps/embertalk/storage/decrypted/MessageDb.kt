package edu.kit.tm.ps.embertalk.storage.decrypted

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class MessageDb : RoomDatabase() {

    abstract fun dao(): MessageDao

    companion object {
        @Volatile
        private var Instance: MessageDb? = null

        fun getDb(context: Context): MessageDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, MessageDb::class.java, "message_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}