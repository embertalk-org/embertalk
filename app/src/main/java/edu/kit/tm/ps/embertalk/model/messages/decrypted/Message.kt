package edu.kit.tm.ps.embertalk.model.messages.decrypted

import android.os.Parcelable
import androidx.room.Entity
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import kotlinx.parcelize.Parcelize
import java.nio.ByteBuffer

@Parcelize
@Entity(tableName = "messages", primaryKeys = ["content","timestamp"])
data class Message(
    val content: String,
    val mine: Boolean = false,
    val recipient: String?,
    val epoch: Long,
    val timestamp: Long,
): Parcelable {
    fun encode(transformer: (ByteArray) -> ByteArray): EncryptedMessage {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES)
            .putInt(content.hashCode()).array() + content.encodeToByteArray()
        val transformed = transformer.invoke(bytes)
        return EncryptedMessage(hash = transformed.contentHashCode(), bytes = transformed, epoch = epoch)
    }

    companion object {
        fun decode(encryptedMessage: EncryptedMessage, epoch: Long, transformer: (ByteArray) -> Array<ByteArray>): Message? {
            val transformed = transformer.invoke(encryptedMessage.bytes)
            for (decryptionResult in transformed) {
                val buffer = ByteBuffer.wrap(decryptionResult)
                val contentHash = buffer.int
                val contentBytes = ByteArray(buffer.remaining())
                buffer.get(contentBytes)
                val content = contentBytes.decodeToString()
                if (contentHash == content.hashCode()) {
                    return Message(content = content, epoch = epoch, recipient = null, timestamp = System.currentTimeMillis())
                }
            }
            return null
        }
    }
}