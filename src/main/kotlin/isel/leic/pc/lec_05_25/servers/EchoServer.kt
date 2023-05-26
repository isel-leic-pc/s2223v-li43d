package isel.leic.pc.lec_05_25.servers

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

class EchoServer(private val port : Int) {
    private val pool = Executors.newCachedThreadPool()

    /**
     * The server accept and process loop
     */
    fun run() {
        val servSocket = ServerSocket()
        try {
            servSocket.bind(InetSocketAddress("0.0.0.0", port))
            while (true) {
                val clientSocket = servSocket.accept()

                // Note this server creates an unbounded number of threads
                // Do not do this at home!
                pool.submit() {
                    logger.info("client ${clientSocket.remoteSocketAddress} connected")
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
    fun processClient(client: Socket) {
        val reader =
            BufferedReader(InputStreamReader(client.getInputStream()))
        val writer =
            PrintWriter(client.getOutputStream())
        try {
            do {
                val line = reader.readLine()

                if (line == null || line.equals("exit")) {
                    writer.println("Bye")
                    writer.flush()
                    break;
                }
                writer.println(line)
                writer.flush()
            } while (true)
        } finally {
            client.close()
            logger.info("client $client  disconnected")
        }
    }

}

private fun main() {
    val server = EchoServer(8080)
    server.run()
}