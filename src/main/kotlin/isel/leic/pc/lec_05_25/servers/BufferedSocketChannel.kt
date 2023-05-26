package isel.leic.pc.coroutines.servers

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.AsynchronousSocketChannel

class BufferedSocketChannel(
    private val _channel: AsynchronousSocketChannel,
    bufCapacity : Int = BYTEBUF_SIZE)  : Closeable {

    val channel
        get() = _channel

    companion object {
        private val BYTEBUF_SIZE = 512
        private val CHARBUF_SIZE = 1024
        private val MAX_LINE = 256
        private val logger = LoggerFactory.getLogger(BufferedSocketChannel::class.java)
    }

    private val inBuffer = ByteBuffer.allocate(bufCapacity)
    private val outBuffer = ByteBuffer.allocate(Math.max(CHARBUF_SIZE, bufCapacity))
    private val decoder = Charsets.UTF_8.newDecoder()
    private val encoder = Charsets.UTF_8.newEncoder()
    private val chars = CharBuffer.allocate(Math.max(CHARBUF_SIZE, (bufCapacity*2)))
    private var previousStart = 0

    private fun lineTermination() : Int {
        for(i in (previousStart until chars.position())) {
            if (chars[i] == '\n') return i
        }
        return -1
    }

    private fun retrieveStringFromCharBuffer(size: Int, discardEol : Boolean = true) : String {
        val lineSize = if (discardEol) size-2 else size
        val charArray = CharArray(lineSize)
        chars.flip()

        chars.get(charArray, 0, lineSize)
        if (discardEol) {
            chars.get() // CR
            chars.get() // LF
        }

        chars.compact()
        previousStart = 0
        return String(charArray, 0, lineSize )
    }

    private suspend fun moreBytesToInputBuffer(fill: (ByteBuffer) -> Int): Boolean {
        var nBytes = fill(inBuffer)
        if (nBytes == -2)  nBytes = _channel.readSuspend(inBuffer)
        logger.info("read $nBytes bytes  from channel ${_channel.localAddress}")

        return ( nBytes > 0)
    }

    private fun decodeBytesInInputBuffer() {
        inBuffer.flip()
        if (chars.position() == 0)
            previousStart = 0;
        else
            // to avoid breaks between CR and LF
            previousStart = chars.position() -1

        decoder.decode(inBuffer, chars, false)
        inBuffer.compact()
    }

    suspend fun readLine(fill: (ByteBuffer) -> Int = {-2}) : String? {

        while(true) {
            val eolIndex = lineTermination()
            if (eolIndex > 0) {
                return retrieveStringFromCharBuffer(eolIndex+1)
            }
            else if (chars.position() >= MAX_LINE) {
                return retrieveStringFromCharBuffer(MAX_LINE, discardEol = false)
            }
            if (!moreBytesToInputBuffer(fill)) return null
            decodeBytesInInputBuffer()
        }
    }

    suspend fun writeLine(str: String)  {
        outBuffer.put(encoder.encode(CharBuffer.wrap(str.toCharArray())))
        outBuffer.put(13)
        outBuffer.put(10)
        outBuffer.flip()
        _channel.writeSuspend(outBuffer)
        outBuffer.clear()
    }

    override fun close() {
        _channel.shutdownOutput()
        _channel.shutdownInput()
        _channel.close()
    }
}