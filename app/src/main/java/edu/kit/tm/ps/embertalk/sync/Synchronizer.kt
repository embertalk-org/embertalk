package edu.kit.tm.ps.embertalk.sync

import android.util.Log
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.storage.MessageStore
import edu.kit.tm.ps.embertalk.sync.bluetooth.Protocol
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Synchronizer {
    private const val TAG = "StreamSync"

    val store = MessageStore()

    fun bidirectionalSync(inputStream: InputStream, outputStream: OutputStream) {
        Log.d(TAG, "Starting sync")

        val source = Okio.buffer(Okio.source(inputStream))
        val sink = Okio.buffer(Okio.sink(outputStream))

        try {
            handshake(source, sink)
            Log.d(TAG, "Handshake successful")
        } catch (e: IOException) {
            Log.e(TAG, "Handshake failed", e)
            return
        }

        val myHashes = store.hashes()
        val theirHashes = try {
            exchangeHashes(myHashes, source, sink)
        } catch (e: IOException) {
            Log.e(TAG, "Hash exchange failed", e)
            return
        }

        val messagesToSend = store.messagesExcept(theirHashes)
        val theirMessages = HashSet<Message>()

        try {
            exchangeMessages(messagesToSend, theirMessages, source, sink)
            Log.d(TAG, "Exchanged messages")
            theirMessages.forEach { store.save(it) }
            Log.d(TAG, "Synced successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Message exchange failed", e)
        }
    }

    private fun exchangeHashes(
        myHashes: Set<Int>,
        source: BufferedSource,
        sink: BufferedSink
    ): Set<Int> {
        Protocol.writeHashes(sink, myHashes)

        val msgType = Protocol.readMessageType(source)
        if (msgType != Protocol.HASHES_ID) {
            throw IOException("Received HASHES with wrong msgType %s".format(msgType))
        }

        return Protocol.readHashes(source)
    }

    private fun handshake(source: BufferedSource, sink: BufferedSink) {
        Protocol.writeHello(sink)

        val msgType = Protocol.readMessageType(source)
        if (msgType != Protocol.HELLO_ID) {
            throw IOException("Received HELLO with wrong msgType %s".format(msgType))
        }
        val protocolName = Protocol.readHello(source)
        if (protocolName != Protocol.PROTOCOL_NAME) {
            throw IOException("Protocol \"$protocolName\" not supported")
        }
    }

    private fun exchangeMessages(myMessages: Set<Message>, theirMessages: MutableSet<Message>, source: BufferedSource, sink: BufferedSink) {
        myMessages.forEach {
            Protocol.writeMessage(sink, it)
        }
        Log.d(TAG, "Wrote %s message(s)".format(myMessages.size))
        Protocol.writeGoodBye(sink)

        var messageType = Protocol.readMessageType(source)

        while (messageType == Protocol.MESSAGE_ID) {
            val msg = Protocol.readMessage(source)
            theirMessages.add(msg)
            messageType = Protocol.readMessageType(source)
        }
    }
}