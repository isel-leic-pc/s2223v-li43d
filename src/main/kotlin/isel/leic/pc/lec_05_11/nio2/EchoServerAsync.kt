package isel.leic.pc.lec_05_11.nio2

import mu.KotlinLogging
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Executors
import isel.leic.pc.lec_05_16.*
import java.nio.channels.AsynchronousChannelGroup

private val logger = KotlinLogging.logger {}

class EchoServerAsync(private val port: Int) {

    private val BUF_SIZE  = 1024
    private val exitCmd = "exit"
    private val byeMsg = "bye"
    private val charSet = Charsets.UTF_8
    private val decoder = charSet.newDecoder()


    private val pool = Executors.newCachedThreadPool()
    /**
     * The server accept and process loop
     */
    fun run() {

        val group =
            AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())

        val servSocket = AsynchronousServerSocketChannel.open(group)
        servSocket.bind(InetSocketAddress("0.0.0.0", port))

        fun acceptLoop() {
            servSocket.accept() { err, client ->
                logger.info("new client accepted from ${client?.remoteAddress}")

                if (err != null) {
                    logger.error("error on accept")
                }
                else {
                    client?.apply { processClient(this)}
                    acceptLoop()
                }
            }
        }

        acceptLoop()
    }

    /**
     * The client session processing loop
     */
    fun processClient(client: AsynchronousSocketChannel) {
        val buffer = ByteBuffer.allocate(BUF_SIZE)


        fun isExitCmd() : Boolean{
            val text = decoder.decode(buffer).toString()
            buffer.rewind()
            return text.equals(exitCmd)
        }

        fun putBuffer(text: String) {
            buffer.clear()

            buffer.put(charSet.encode(text))
            buffer.flip()
        }

        fun bye() {
            putBuffer(byeMsg)
            client.write(buffer).get()
            buffer.clear()
            Thread.sleep(1000)
            client.shutdownOutput()
            client.close()
        }

        fun clientLoop() {
            client.read(buffer) { err, nBytes ->
                if (err != null) {
                    closeConnection(client)
                } else if (nBytes <= 0) {
                    closeConnection(client)
                } else {
                    logger.info("$nBytes received from ${client.remoteAddress}")
                    buffer.flip()
                    if (isExitCmd()) {
                        bye()
                    } else {
                        client.write(buffer) { err, nbytes ->
                            buffer.clear()
                            clientLoop()
                        }
                    }
                }
            }
        }

        clientLoop()
    }

}

private fun main() {
    val server = EchoServerAsync(8080)

    server.run()

    readln()

    println("press return to terminate...")
}