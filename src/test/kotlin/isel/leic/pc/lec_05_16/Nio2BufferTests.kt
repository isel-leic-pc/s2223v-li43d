package isel.leic.pc.lec_05_16

import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer

class Nio2BufferTests {

    @Test
    fun byteBufferStateTest() {
        val buffer = ByteBuffer.allocate(1024)

        val bytes = ByteArray(3) { it -> it.toByte() }

        buffer.put(bytes)
        buffer.put(bytes)
        for(i in  (0 until buffer.position()))
            println(buffer.get(i))
    }

    @Test
    fun puBufferStateTest() {
        val buffer = ByteBuffer.allocate(1024);

        Assert.assertEquals(0, buffer.position())
        Assert.assertEquals(1024, buffer.limit())

        val bytes = ByteArray(3) { it -> it.toByte() }
        buffer.put(bytes)
        buffer.put(bytes)

        Assert.assertEquals(6, buffer.position())
        Assert.assertEquals(1024, buffer.limit())

        buffer.rewind()
        Assert.assertEquals(0, buffer.position())
        Assert.assertEquals(1024, buffer.limit())

        buffer.put(bytes)

        buffer.flip()

        Assert.assertEquals(0, buffer.position())
        Assert.assertEquals(3, buffer.limit())

        buffer.clear()

        Assert.assertEquals(0, buffer.position())
        Assert.assertEquals(1024, buffer.limit())
    }

}