package edu.kit.tm.ps.embertalk.storage.encrypted

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class EncryptedMessage(
    @PrimaryKey
    val hash: Int,
    val bytes: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}