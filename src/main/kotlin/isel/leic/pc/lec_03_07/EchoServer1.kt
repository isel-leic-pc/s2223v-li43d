/**
 * A new echo server version with client identification
 */

package isel.leic.pc.lec_03_06

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import mu.KotlinLogging


private val logger = KotlinLogging.logger {}

class EchoServer1(private val port : Int) {

    /**
     * The server accept and process loop
     */
    fun run() {
        val servSocket = ServerSocket()
        var newClientId = 1

        try {
            servSocket.bind(InetSocketAddress("0.0.0.0", port))
            while (true) {
                val clientSocket = servSocket.accept()

                newClientId++

                val localClientId = newClientId

                // Note this server creates an unbounded number of threads
                // Do not do this at home!
                thread {
                    logger.info("client ${clientSocket.remoteSocketAddress} connected")
                    processClient(clientSocket, localClientId)
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
    fun processClient(client: Socket, clientId : Int) {
        val reader =
            BufferedReader(InputStreamReader(client.getInputStream()))
        val writer =
            PrintWriter(client.getOutputStream())
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
            logger.info("client $clientId  disconnected")
        }
    }
	


	private fun main() {
		val server = EchoServer1(8080)
		server.run()
	}
}