package isel.leic.pc.lec_05_25


import isel.leic.pc.lec_05_25.servers.BufferedSocketChannel
import kotlinx.coroutines.runBlocking
import org.junit.Test


import java.lang.IllegalStateException
import java.nio.channels.AsynchronousSocketChannel

/*
class BufferedSocketChannelTests {
    @Test
    public fun `buffer with a single line test`() {
        val channel = AsynchronousSocketChannel.open()
        val buffer = BufferedSocketChannel(channel)

        runBlocking {
            val line = buffer.readLine { bb ->
                val bytes = byteArrayOf(65, 66, 67, 13, 10)
                bb.put(bytes)
                5
            }
            println(line)
        }

    }

    @Test
    public fun `buffer with a line after third fill test`() {
        val channel = AsynchronousSocketChannel.open()
        val buffer = BufferedSocketChannel(channel)

        runBlocking {
            var state = 0
            val line = buffer.readLine { bb ->
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
                        bb.put(byteArrayOf(10))
                        state = -1
                        1
                    }
                    -1 -> {
                        throw IllegalStateException()
                    }
                }

                5
            }
            println(line)
        }

    }

    @Test
    public fun `buffer with two lines and after third fill test`() {
        val channel = AsynchronousSocketChannel.open()
        val buffer = BufferedSocketChannel(channel)

        runBlocking {
            var state = 0
            val line1 = buffer.readLine { bb ->
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
                        bb.put(byteArrayOf(10, 70, 71, 13, 10))
                        state = -1
                        5
                    }
                    -1 -> {
                        throw IllegalStateException()
                    }
                }

                5
            }
            println(line1)
            val line2 = buffer.readLine { bb -> -1 }
            println(line2)
        }

    }
}

 */