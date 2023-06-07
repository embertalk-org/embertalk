package edu.kit.tm.ps.embertalk.sync.bluetooth

import android.util.Log
import edu.kit.tm.ps.embertalk.storage.Message
import okio.BufferedSink
import okio.BufferedSource
import java.nio.charset.Charset

object Protocol {

    const val TAG = "Protocol"

    const val PROTOCOL_NAME = "Ember"
    private val DEFAULT_CHARSET = Charset.defaultCharset()

    const val HELLO_ID = 0
    const val HASHES_ID = 1
    const val MESSAGE_ID = 2
    const val GOODBYE_ID = 3

    fun writeHello(sink: BufferedSink) {
        sink.writeByte(HELLO_ID)
        sink.writeByte(PROTOCOL_NAME.length)
        sink.writeString(PROTOCOL_NAME, DEFAULT_CHARSET)
        sink.flush()
        Log.d(TAG, "Wrote hello")
    }

    fun writeHashes(sink: BufferedSink, hashes: Set<Int>) {
        sink.writeByte(HASHES_ID)
        sink.writeInt(hashes.size)
        hashes.forEach { sink.writeInt(it) }
        sink.flush()
        Log.d(TAG, "Wrote %s hashes".format(hashes.size))
    }

    fun writeMessage(sink: BufferedSink, message: Message) {
        sink.writeByte(MESSAGE_ID)
        sink.writeInt(message.bytes.size)
        sink.write(message.bytes)
        sink.flush()
        Log.d(TAG, "Wrote Message")
    }

    fun writeGoodBye(sink: BufferedSink) {
        sink.writeByte(GOODBYE_ID)
        sink.flush()
        Log.d(TAG, "Wrote Goodbye")
    }

    fun readMessageType(source: BufferedSource): Int {
        val msgType = source.readByte().toInt()
        Log.d(TAG, "Read msgtype %s".format(msgType))
        return msgType
    }

    fun readHello(source: BufferedSource): String {
        val protocolNameLength = source.readByte()
        val protocolName = source.readString(protocolNameLength.toLong(), DEFAULT_CHARSET)
        Log.d(TAG, "Read hello with protocol name %s".format(protocolName))
        return protocolName
    }

    fun readHashes(source: BufferedSource): Set<Int> {
        val numHashes = source.readInt()
        val hashes = HashSet<Int>()
        for (i in 1..numHashes) {
            val hash = source.readInt()
            hashes.add(hash)
            Log.d(TAG, "Read hash %s".format(i))
        }
        Log.d(TAG, "Read %s hashes".format(numHashes))
        return hashes
    }

    fun readMessage(source: BufferedSource): Message {
        val msgLength = source.readInt()
        val bytes = source.readByteArray(msgLength.toLong())
        Log.d(TAG, "Read message of length %s".format(msgLength))
        return Message(bytes)
    }
}