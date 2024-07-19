package edu.kit.tm.ps.embertalk.sync

import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import org.junit.Assert
import org.junit.Test

class ProtocolTests {

    @Test
    fun testHashes() {
        val hashes = setOf(1, 127162, 78623461, 827367816)
        val serialized = Protocol.fromHashes(hashes)
        val deserialized = Protocol.toHashes(serialized)
        Assert.assertEquals(hashes, deserialized)
    }

    @Test
    fun testMessages() {
        val messages = setOf(
            "dawd9waod8aw8z7diaw",
            "d7u8wad87awijmc",
            "9028edjmcakwiud17"
        ).map {
            val contentBytes = it.toByteArray()
            EncryptedMessage(contentBytes.contentHashCode(), contentBytes, 0)
        }
        val serialized = Protocol.fromMessages(messages)
        val deserialized = Protocol.toMessages(serialized)
            .map { EncryptedMessage(it.hash, it.bytes, 0) }
        Assert.assertEquals(messages, deserialized)
    }
}