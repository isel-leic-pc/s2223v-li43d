package isel.leic.pc.lec_05_30.servers

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

private val logger = KotlinLogging.logger {}

class EchoServer(private val port : Int) {
    val sessionsParent = SupervisorJob()
    val sessionsScope = CoroutineScope(sessionsParent + Dispatchers.IO)
    val serverSocketChannel = AsynchronousServerSocketChannel.open()

    val acceptScope = CoroutineScope(Dispatchers.IO+ Job())
    /**
     * The server accept and process loop
     */
    fun run() {
        acceptScope.launch {
            acceptLoop(serverSocketChannel)
        }
    }

    suspend fun acceptLoop(serverChannel: AsynchronousServerSocketChannel) {

        try {
            serverChannel.bind(InetSocketAddress("0.0.0.0", port))
            while (true) {
                val clientChannel = serverChannel.acceptSuspend()

                // Note this server creates an unbounded number of threads
                // Do not do this at home!
                sessionsScope.launch {
                    logger.info("client ${clientChannel.remoteAddress} connected")
                    processClient(BufferedSocketChannel(clientChannel))
                }
            }
        } catch (e: Exception) {
            serverChannel.close()
            logger.error("unrecovered error in server:${e.message}")
        }
    }

    /**
     * The client session processing loop
     */
    suspend fun processClient(channel: BufferedSocketChannel) {

        try {
            do {
                val line = channel.readLine()

                if (line == null || line.equals("exit")) {
                    channel.writeLine("Bye")
                    break;
                }
                channel.writeLine(line)
            } while (true)
        } finally {
            channel.close()
            logger.info("client $channel  disconnected")
        }
    }


    fun shutdown() {
        logger.info("Start shutdown")
        runBlocking {
            sessionsParent.cancel()
            sessionsParent.join()
        }
        logger.info("End shutdown")
    }

}

private fun main() {
    val server = EchoServer(8080)
    server.run()
    readln()
    server.shutdown()

}