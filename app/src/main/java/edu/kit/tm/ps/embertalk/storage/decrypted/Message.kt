package edu.kit.tm.ps.embertalk.storage.decrypted

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessage
import kotlinx.parcelize.Parcelize
import java.nio.ByteBuffer

@Parcelize
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val mine: Boolean = false,
    val epoch: Long,
): Parcelable {
    fun encode(transformer: (ByteArray) -> ByteArray): EncryptedMessage {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES)
            .putInt(content.hashCode()).array() + content.encodeToByteArray()
        val transformed = transformer.invoke(bytes)
        return EncryptedMessage(hash = transformed.contentHashCode(), bytes = transformed)
    }

    companion object {
        fun decode(encryptedMessage: EncryptedMessage, epoch: Long, transformer: (ByteArray) -> ByteArray): Message? {
            val transformed = transformer.invoke(encryptedMessage.bytes)
            val buffer = ByteBuffer.wrap(transformed)
            val contentHash = buffer.int
            val contentBytes = ByteArray(buffer.remaining())
            buffer.get(contentBytes)
            val content = contentBytes.decodeToString()
            return if (contentHash == content.hashCode()) Message(content = content, epoch = epoch) else null
        }
    }
}