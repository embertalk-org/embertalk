package edu.kit.tm.ps.embertalk.storage

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(val bytes: ByteArray): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}