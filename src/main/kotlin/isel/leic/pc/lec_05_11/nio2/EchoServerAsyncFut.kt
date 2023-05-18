package isel.leic.pc.lec_05_11.nio2


import mu.KotlinLogging
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

// A bad implementation of echo server, using operations
// on asynchronous channels that return futures.
// The implementation is bad because it blocks the
// threads from the pool on future get operations
// Essentially it behaves very similar to the old version
// that uses synchronous socket I/O.
class EchoServerAsyncFut(private val port: Int) {

    private val BUF_SIZE  = 1024
    private val exitCmd = "exit"
    private val byeMsg = "bye"
    private val charSet = Charsets.UTF_8
    private val decoder = charSet.newDecoder()

    private fun isExitCmd(buffer : ByteBuffer) : Boolean{
        val text = decoder.decode(buffer).toString()
        buffer.rewind()
        return text.equals(exitCmd)
    }

    fun putBuffer(buffer: ByteBuffer, text: String) {
        buffer.clear()

        buffer.put(charSet.encode(text))
        buffer.flip()
    }

    fun bye(buffer : ByteBuffer, client : AsynchronousSocketChannel) {
        putBuffer(buffer, byeMsg)
        client.write(buffer).get()
        buffer.clear()
        sleep(1000)
        client.shutdownOutput()
        client.close()
    }

    private val pool = Executors.newCachedThreadPool()
    /**
     * The server accept and process loop
     */
    fun run() {
        val servSocket = AsynchronousServerSocketChannel.open()
        try {
            servSocket.bind(InetSocketAddress("0.0.0.0", port))
            while (true) {
                val clientSocket = servSocket.accept().get()


                // Note this server creates an unbounded number of threads
                // Do not do this at home!
                pool.submit() {
                    logger.info("client ${clientSocket.remoteAddress} connected")
                    processClient(clientSocket)
                }
            }
        } catch (e: Exception) {
            servSocket.close()
            logger.error("unrecovered error in server:${e.message}")
        }
    }

    /**
     * The client session processing loop
     */
    fun processClient(client: AsynchronousSocketChannel) {
        val buffer = ByteBuffer.allocate(BUF_SIZE)
        try {
            do {
                val nbytes = client.read(buffer).get()
                buffer.flip()
                if (nbytes < 0 )
                    break;
                if (isExitCmd(buffer)) {
                    bye(buffer, client)
                    break
                }
                client.write(buffer).get()
                buffer.clear()

            } while (true)
        } finally {

            client.close()
            logger.info("client $client  disconnected")
        }
    }

}


private fun main() {
    val server = EchoServerAsyncFut(8080)

    server.run()
}