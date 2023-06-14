package edu.kit.tm.ps.embertalk.storage.decoded

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.kit.tm.ps.embertalk.storage.encrypted.Message
import kotlinx.parcelize.Parcelize
import java.nio.ByteBuffer

@Parcelize
@Entity(tableName = "messages")
data class DecodedMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val epoch: Long,
): Parcelable {
    fun encode(): Message {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES)
            .putInt(content.hashCode()).array() + content.encodeToByteArray()
        return Message(hash = bytes.contentHashCode(), bytes = bytes)
    }

    companion object {
        fun decode(message: Message, epoch: Long): DecodedMessage? {
            val buffer = ByteBuffer.wrap(message.bytes)
            val contentHash = buffer.int
            val contentBytes = ByteArray(buffer.remaining())
            buffer.get(contentBytes)
            val content = contentBytes.decodeToString()
            return if (contentHash == content.hashCode()) DecodedMessage(content = content, epoch = epoch) else null
        }
    }
}