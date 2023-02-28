package isel.leic.pc

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


private val logger = KotlinLogging.logger {}

class EchoServer(private val port : Int) {

    /**
     * The server accept and process loop
     */
    fun run() {
        val servSocket = ServerSocket()
        try {
            servSocket.bind(InetSocketAddress("0.0.0.0", port))
            while (true) {
                val clientSocket = servSocket.accept()
                logger.info("client ${clientSocket.remoteSocketAddress} connected")

                // Note this server creates an unbounded number of threads
                // Do not do this at home!
                thread {
                    processClient(clientSocket)
                }
            }
        }
        catch(e: Exception) {
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
            PrintWriter( client.getOutputStream())
        try {
            do {
                val line = reader.readLine()

                if (line == null || line.equals("exit"))
                    break;

                writer.println(line)
                writer.flush()
            }
            while(true)
        }
        finally {
            writer.println("Bye")
            writer.flush()
            client.close()
            logger.info("client ${client.remoteSocketAddress} disconnected")
        }
    }
}

private fun main() {
    val server = EchoServer(8080)
    server.run()
}