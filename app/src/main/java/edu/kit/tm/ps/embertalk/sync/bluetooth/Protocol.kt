package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.util.Log
import edu.kit.tm.ps.embertalk.storage.Message
import java.io.DataInputStream
import java.io.DataOutputStream

class Protocol(private val inputStream: DataInputStream, private val outputStream: DataOutputStream) {

    companion object {
        const val TAG = "Protocol"
        const val PROTOCOL_NAME = "Ember"
        const val HELLO_ID = 0
        const val HASHES_ID = 1
        const val MESSAGE_ID = 2
        const val GOODBYE_ID = 3
    }

    fun writeHello() {
        outputStream.writeByte(HELLO_ID)
        outputStream.writeString(PROTOCOL_NAME)
        outputStream.flush()
        Log.d(TAG, "Wrote hello")
    }

    fun writeHashes(hashes: Set<Int>) {
        outputStream.writeByte(HASHES_ID)
        outputStream.writeInt(hashes.size)
        hashes.forEach {
            outputStream.writeInt(it)
            Log.d(TAG, "Wrote hash %s".format(it))
        }
        outputStream.flush()
        Log.d(TAG, "Wrote %s hashes".format(hashes.size))
    }

    fun writeMessage(message: Message) {
        outputStream.writeByte(MESSAGE_ID)
        outputStream.writeByteArray(message.bytes)
        outputStream.flush()
        Log.d(TAG, "Wrote Message")
    }

    fun writeGoodBye() {
        outputStream.writeByte(GOODBYE_ID)
        outputStream.flush()
        Log.d(TAG, "Wrote Goodbye")
    }

    fun readMessageType(): Int {
        val msgType = inputStream.readByte().toInt()
        Log.d(TAG, "Read msgtype %s".format(msgType))
        return msgType
    }

    fun readHello(): String {
        val protocolName = inputStream.readString()
        Log.d(TAG, "Read hello with protocol name %s".format(protocolName))
        return protocolName
    }

    fun readHashes(): Set<Int> {
        val numHashes = inputStream.readInt()
        val hashes = HashSet<Int>()
        for (i in 0 until numHashes) {
            val hash = inputStream.readInt()
            hashes.add(hash)
            Log.d(TAG, "Read hash %s".format(hash))
        }
        Log.d(TAG, "Read %s hashes".format(numHashes))
        return hashes
    }

    fun readMessage(): Message {
        val bytes = inputStream.readByteArray()
        Log.d(TAG, "Read message of length %s".format(bytes.size))
        return Message(hash = bytes.contentHashCode(), bytes = bytes)
    }
}

private fun DataOutputStream.writeString(string: String) {
    this.writeByteArray(string.encodeToByteArray())
}

private fun DataInputStream.readString(): String {
    return this.readByteArray().decodeToString()
}

private fun DataInputStream.readByteArray(): ByteArray {
    val bytes = ByteArray(this.readInt())
    for (i in bytes.indices) {
        bytes[i] = this.readByte()
    }
    return bytes
}


private fun DataOutputStream.writeByteArray(bytes: ByteArray) {
    this.writeInt(bytes.size)
    for (byte in bytes) {
        this.writeByte(byte.toInt())
    }
}