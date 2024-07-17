package edu.kit.tm.ps.embertalk.model.messages.decrypted

import android.os.Parcelable
import androidx.room.Entity
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.UUID

@Parcelize
@Entity(tableName = "messages", primaryKeys = ["content","timestamp"])
data class Message(
    val content: String,
    val senderUserId: UUID,
    val recipient: UUID,
    val timestamp: Long,
): Parcelable {

    private fun toByteArray(): ByteArray {
        val content = JSONObject().apply {
            this.put("content", content)
            this.put("sender", senderUserId.toString())
            this.put("recipient", recipient.toString())
            this.put("timestamp", timestamp)
        }
        val contentBytes = content.toString().encodeToByteArray()
        return ByteBuffer.allocate(Int.SIZE_BYTES)
            .putInt(contentBytes.contentHashCode()).array() + contentBytes
    }

    fun encode(transformer: (ByteArray) -> ByteArray): EncryptedMessage {
        val bytes = this.toByteArray()
        val transformed = transformer.invoke(bytes)
        return EncryptedMessage(
            hash = transformed.contentHashCode(),
            bytes = transformed,
            timestamp = System.currentTimeMillis()
        )
    }

    companion object {
        fun decode(encryptedMessage: EncryptedMessage, transformer: (ByteArray) -> Array<ByteArray>): Message? {
            val transformed = transformer.invoke(encryptedMessage.bytes)
            for (decryptionResult in transformed) {
                val buffer = ByteBuffer.wrap(decryptionResult)
                val contentHash = buffer.int
                val contentBytes = ByteArray(buffer.remaining())
                buffer.get(contentBytes)
                val json = JSONObject(contentBytes.decodeToString())
                if (contentHash == contentBytes.contentHashCode()) {
                    return Message(
                        content = json.getString("content"),
                        senderUserId = UUID.fromString(json.getString("sender")),
                        recipient = UUID.fromString(json.getString("recipient")),
                        timestamp = json.getLong("timestamp")
                    )
                }
            }
            return null
        }
    }
}