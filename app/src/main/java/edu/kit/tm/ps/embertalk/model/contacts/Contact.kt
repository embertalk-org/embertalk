package edu.kit.tm.ps.embertalk.model.contacts

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "contacts")
data class Contact (
    val name: String,
    @PrimaryKey
    val pubKey: String,
): Parcelable