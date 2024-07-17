package edu.kit.tm.ps.embertalk.model.messages.encrypted

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class EncryptedMessage(
    @PrimaryKey
    val hash: Int,
    val bytes: ByteArray,
    val timestamp: Long,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage

        return this.hash == other.hash
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}