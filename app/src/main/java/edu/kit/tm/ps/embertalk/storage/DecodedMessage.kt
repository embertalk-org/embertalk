package edu.kit.tm.ps.embertalk.storage

import java.nio.ByteBuffer

data class DecodedMessage(
    val content: String,
    val epoch: Long,
) {
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
            return if (contentHash == content.hashCode()) DecodedMessage(content, epoch) else null
        }
    }
}