package edu.kit.tm.ps.embertalk.storage.decoded

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DecodedMessage::class], version = 1, exportSchema = false)
abstract class DecodedMessageDb : RoomDatabase() {

    abstract fun dao(): DecodedMessageDao

    companion object {
        @Volatile
        private var Instance: DecodedMessageDb? = null

        fun getDb(context: Context): DecodedMessageDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DecodedMessageDb::class.java, "decoded_message_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}