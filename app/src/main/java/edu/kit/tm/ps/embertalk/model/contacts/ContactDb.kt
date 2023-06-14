package edu.kit.tm.ps.embertalk.model.contacts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDb : RoomDatabase() {

    abstract fun dao(): ContactDao

    companion object {
        @Volatile
        private var Instance: ContactDb? = null

        fun getDb(context: Context): ContactDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ContactDb::class.java, "contact_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}