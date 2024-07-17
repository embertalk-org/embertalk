package edu.kit.tm.ps.embertalk.sync

import android.util.Log
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

object Protocol {
    const val TAG = "Protocol"

    fun fromMessages(messages: Collection<EncryptedMessage>): ByteArray {
        val buffer = ByteArrayOutputStream()
        val writer = DataOutputStream(buffer)
        writer.writeInt(messages.size)
        for (message in messages) {
            val msgBytes = message.bytes
            writer.writeInt(msgBytes.size)
            for (byte in msgBytes) {
                writer.writeByte(byte.toInt())
            }
        }
        writer.close()
        return buffer.toByteArray()
    }

    fun toMessages(bytes: ByteArray): Collection<EncryptedMessage> {
        val messages: MutableSet<EncryptedMessage> = HashSet()
        val buffer = ByteBuffer.wrap(bytes)
        val size = buffer.getInt()
        for (i in 0 until size) {
            val msgSize = buffer.getInt()
            val msgBuffer = ByteArray(msgSize)
            for (j in 0 until msgSize) {
                msgBuffer[j] = buffer.get()
            }
            messages.add(EncryptedMessage(hash = msgBuffer.contentHashCode(), bytes = msgBuffer, timestamp = System.currentTimeMillis()))
        }
        return messages
    }

    fun fromHashes(hashes: Collection<Int>): ByteArray {
        val buffer = ByteArrayOutputStream()
        val writer = DataOutputStream(buffer)
        writer.writeInt(hashes.size)
        hashes.forEach {
            writer.writeInt(it)
            Log.d(TAG, "Wrote hash %s".format(it))
        }
        writer.close()
        return buffer.toByteArray()
    }

    fun toHashes(bytes: ByteArray): Set<Int> {
        val hashes: MutableSet<Int> = HashSet()
        val buffer = ByteBuffer.wrap(bytes)
        val size = buffer.getInt()
        for (i in 0 until size) {
            hashes.add(buffer.getInt())
        }
        return hashes
    }
}