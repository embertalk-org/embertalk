package edu.kit.tm.ps.embertalk.model.contacts

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "contacts")
data class Contact (
    val name: String,
    @PrimaryKey
    val userId: UUID,
    val pubKey: String,
): Parcelable