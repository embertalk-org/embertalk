package edu.kit.tm.ps.embertalk.sync

import android.util.Log
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.storage.MessageRepository
import edu.kit.tm.ps.embertalk.sync.bluetooth.Protocol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class Synchronizer(private val messageRepository: MessageRepository) {

    fun bidirectionalSync(inputStream: InputStream, outputStream: OutputStream): Boolean {
        Log.d(TAG, "Starting sync")

        val protocol = Protocol(DataInputStream(inputStream), DataOutputStream(outputStream))

        try {
            handshake(protocol)
            Log.d(TAG, "Handshake successful")
        } catch (e: IOException) {
            Log.e(TAG, "Handshake failed", e)
            return false
        }

        val myHashes = runBlocking { messageRepository.hashes().first() }
        val theirHashes = try {
            exchangeHashes(myHashes.toSet(), protocol)
        } catch (e: IOException) {
            Log.e(TAG, "Hash exchange failed", e)
            return false
        }

        val messagesToSend = runBlocking { messageRepository.allExcept(theirHashes).first() }.toSet()
        Log.d(TAG, "Retrieved %s messages to send".format(messagesToSend.size))
        val theirMessages = HashSet<Message>()

        return try {
            exchangeMessages(messagesToSend, theirMessages, protocol)
            Log.d(TAG, "Exchanged messages")
            theirMessages.forEach { runBlocking { messageRepository.insert(it) } }
            Log.d(TAG, "Synced successfully")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Message exchange failed", e)
            false
        }
    }

    private fun exchangeHashes(
        myHashes: Set<Int>,
        protocol: Protocol
    ): Set<Int> {
        protocol.writeHashes(myHashes)

        val msgType = protocol.readMessageType()
        if (msgType != Protocol.HASHES_ID) {
            throw IOException("Received HASHES with wrong msgType %s".format(msgType))
        }

        return protocol.readHashes()
    }

    private fun handshake(protocol: Protocol) {
        protocol.writeHello()

        val msgType = protocol.readMessageType()
        if (msgType != Protocol.HELLO_ID) {
            throw IOException("Received HELLO with wrong msgType %s".format(msgType))
        }
        val protocolName = protocol.readHello()
        if (protocolName != Protocol.PROTOCOL_NAME) {
            throw IOException("protocol \"$protocolName\" not supported")
        }
    }

    private fun exchangeMessages(myMessages: Set<Message>, theirMessages: MutableSet<Message>, protocol: Protocol) {
        myMessages.forEach {
            protocol.writeMessage(it)
        }
        Log.d(TAG, "Wrote %s message(s)".format(myMessages.size))
        protocol.writeGoodBye()

        var messageType = protocol.readMessageType()

        while (messageType == Protocol.MESSAGE_ID) {
            val msg = protocol.readMessage()
            theirMessages.add(msg)
            messageType = protocol.readMessageType()
        }
    }

    companion object {
        private const val TAG = "StreamSync"
    }
}