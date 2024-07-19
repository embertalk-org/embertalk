package edu.kit.tm.ps.embertalk.sync.bluetooth

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ServiceUtilsTests {

    private val array = "781281673687d6nwa87dwand5awdnwa9dwa6d8192cf93762v 68i6".toByteArray()

    @Test
    fun testChunking() {
        val buffer = ByteBuffer.wrap(array)
        val result = ByteArrayOutputStream()
        while (buffer.hasRemaining()) {
            result.write(buffer.nextChunk())
        }
        Assert.assertArrayEquals(array, result.toByteArray())
    }
}