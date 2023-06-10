package edu.kit.tm.ps.embertalk.storage

import com.google.gson.Gson

data class DecodedMessage(
    val from: String,
    val content: String
) {
    fun encode(): Message {
        val bytes = Gson().toJson(this, DecodedMessage::class.java).encodeToByteArray()
        return Message(hash = bytes.contentHashCode(), bytes = bytes)
    }

    companion object Decoder {
        fun decode(message: Message): DecodedMessage {
            return Gson().fromJson(message.bytes.decodeToString(), DecodedMessage::class.java)
        }
    }
}