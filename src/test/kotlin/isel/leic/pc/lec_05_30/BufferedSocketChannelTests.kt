package isel.leic.pc.lec_05_30


import isel.leic.pc.lec_05_30.servers.BufferedSocketChannel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class BufferedSocketChannelTests {
    @Test
    public fun `buffer with a single line terminated by crlf`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0
        val fill : (ByteBuffer) -> Int = { bb ->
            when (state) {
                0 -> {
                    val bytes = byteArrayOf(65, 66, 67, 13, 10)
                    bb.put(bytes)
                    state = 1
                    5
                }
                else -> {
                    -1
                }
            }
        }

        val buffer = BufferedSocketChannel(channel, 512,  fill)
        runBlocking {
            val line = buffer.readLine()
            println(line)
            assertEquals("ABC", line)
        }
    }

    @Test
    public fun `buffer with a single line terminated by lf`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0
        val fill : (ByteBuffer) -> Int = { bb ->
            when (state) {
                0 -> {
                    val bytes = byteArrayOf(65, 66, 67, 10)
                    bb.put(bytes)
                    state = 1
                    5
                }
                else -> {
                    -1
                }
            }

        }
        val buffer = BufferedSocketChannel(channel, 512, fill)
        runBlocking {
            val line = buffer.readLine()
            println(line)
            assertEquals("ABC", line)
        }
    }

    @Test
    public fun `buffer with three lines terminated by different line terminators and a last line not terminated`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0
        val fill : (ByteBuffer) -> Int = { bb ->
            when (state) {
                0 -> {
                    val bytes = byteArrayOf(65, 66, 67, 10, 13, 68, 69, 10, 70, 13, 10, 71, 72, 13, 73)
                    bb.put(bytes)
                    state = 1
                    5
                }
                else -> {
                    -1
                }
            }

        }
        val buffer = BufferedSocketChannel(channel, 512, fill)
        runBlocking {
            var line = buffer.readLine()
            println(line)
            assertEquals("ABC", line)
            line = buffer.readLine()
            println(line)
            assertEquals("DE", line)
            line = buffer.readLine()
            println(line)
            assertEquals("F", line)
            line = buffer.readLine()
            println(line)
            assertEquals("GH", line)
            line = buffer.readLine()
            println(line)
            assertEquals("I", line)
            assertNull( buffer.readLine())
        }
    }


    @Test
    public fun `buffer with a line after third fill test`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0
        val fill : (ByteBuffer) -> Int = { bb ->
            when (state) {
                0 -> {
                    bb.put(byteArrayOf(65, 66))
                    state = 1
                    2
                }

                1 -> {
                    bb.put(byteArrayOf(67, 13))
                    state = 2
                    2
                }

                2 -> {
                    bb.put(byteArrayOf(10))
                    state = -1
                    1
                }

                else -> {
                    -1
                }
            }
        }
        val buffer = BufferedSocketChannel(channel, 512, fill)
        runBlocking {
            val line1 = buffer.readLine()
            println(line1)
            assertEquals("ABC", line1)
            val line2 = buffer.readLine()
            assertNull(line2)
        }
    }

    @Test
    public fun `buffer with two lines after third fill test`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0

        val fill : (ByteBuffer) -> Int = { bb ->
            when(state) {
                0 ->  {
                    bb.put(byteArrayOf(65, 66 ))
                    state = 1
                    2
                }
                1 ->  {
                    bb.put(byteArrayOf( 67, 13))
                    state = 2
                    2
                }
                2 ->  {
                    bb.put(byteArrayOf(10, 70, 71, 13))
                    state = 3
                    5
                }
                3 -> {
                    state = -1
                    -1
                }
                else -> {-1}
            }
        }

        val buffer = BufferedSocketChannel(channel,512, fill)

        runBlocking {
            val line1 = buffer.readLine()
            println(line1)
            assertEquals("ABC", line1)
            val line2 = buffer.readLine()
            assertEquals("FG", line2)
            println(line2)
            val line3 = buffer.readLine()
            assertNull(line3)
        }

    }

    @Test
    public fun `buffer with two lines after third fill with different line terminators`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0

        val fill : (ByteBuffer) -> Int = { bb ->
            when(state) {
                0 ->  {
                    bb.put(byteArrayOf(65, 66 ))
                    state = 1
                    2
                }
                1 ->  {
                    bb.put(byteArrayOf( 67, 13))
                    state = 2
                    2
                }
                2 ->  {
                    bb.put(byteArrayOf(10, 70, 71, 10))
                    state = 3
                   4
                }
                3 -> {
                    bb.put(byteArrayOf(13))
                    state = 4
                    1
                }
                else -> {-1}
            }
        }

        val buffer = BufferedSocketChannel(channel,512, fill)

        runBlocking {
            val line1 = buffer.readLine()
            println(line1)
            assertEquals("ABC", line1)
            val line2 = buffer.readLine()
            assertEquals("FG", line2)
            println(line2)
            val line3 = buffer.readLine()
            assertNull(line3)
        }

    }

    @Test
    public fun `buffer with a big line (graeter than max) terminated by crlf`() {
        val channel = AsynchronousSocketChannel.open()
        var state = 0
        val fill : (ByteBuffer) -> Int = { bb ->
            when (state) {
                0 -> {
                    val bytes = byteArrayOf(65, 66, 67, 68,69,70,71, 13, 10)
                    bb.put(bytes)
                    state = 1
                    5
                }
                else -> {
                    -1
                }
            }
        }

        val buffer = BufferedSocketChannel(channel, 512, 5, fill)
        runBlocking {
            var line = buffer.readLine()
            println(line)
            assertEquals("ABCDE", line)
            line = buffer.readLine()
            println(line)
            assertEquals("FG", line)
            assertNull(buffer.readLine())
        }
    }
}