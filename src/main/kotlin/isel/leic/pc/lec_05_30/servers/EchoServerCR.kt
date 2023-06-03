/**
 * A version of the echo server using coroutines and
 * a suspend API to NIO2 socket channels
 */

package isel.leic.pc.lec_05_30.servers

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel

private val logger = KotlinLogging.logger {}

class EchoServerCR(private val port : Int) {
    val sessionsParent = SupervisorJob()
    val sessionsScope = CoroutineScope(sessionsParent + Dispatchers.IO)
    val serverSocketChannel = AsynchronousServerSocketChannel.open()

    val acceptScope = CoroutineScope(Dispatchers.IO+ Job())

    /**
     * The server start launching the acceptLoop coroutine
     * In this implementation it is a regular (non suspend) function,
     * so it can be called by regular code.
     */
    fun start() {
        acceptScope.launch {
            try {
                acceptLoop(serverSocketChannel)
            }
            catch (e: Throwable) {
                serverSocketChannel.close()
                logger.error("unrecoverable error in server:${e.message}")
            }
        }
    }

    private suspend fun launchClient(clientChannel : AsynchronousSocketChannel) {
        sessionsScope.launch {
            logger.info("client ${clientChannel.remoteAddress} connected")
            val bufChannel = BufferedSocketChannel(clientChannel)
            try {
                processClient(bufChannel)
            } catch (e: Throwable) {
                logger.error("unrecovered error in client:${e.message}")
            } finally {
                logger.info("client ${clientChannel.remoteAddress}  disconnected")
                bufChannel.close()
            }
        }
    }

    suspend fun acceptLoop(serverChannel: AsynchronousServerSocketChannel) {
        serverChannel.bind(InetSocketAddress("0.0.0.0", port))
        while (true) {
            val clientChannel = serverChannel.acceptSuspend()
            // launch the coroutine for client processing
            launchClient(clientChannel)
        }
    }

    /**
     * The client session processing loop
     */
    suspend fun processClient(bufChannel: BufferedSocketChannel) {
        do {
            val line = bufChannel.readLine()

            if (line == null || line.equals("exit")) {
                bufChannel.writeLine("Bye")
                break;
            }
            bufChannel.writeLine(line)
        } while (true)
    }


    suspend fun shutdown() {
        logger.info("Start shutdown")
        // force accept loop to terminate by closing the server socket
        serverSocketChannel.close()

        sessionsParent.cancel()
        sessionsParent.join()

        logger.info("End shutdown")
    }

}

private fun main() {
    val server = EchoServerCR(8080)
    server.start()

    println("Press enter to shutdown...")
    readln()

    runBlocking {
        server.shutdown()
    }

}