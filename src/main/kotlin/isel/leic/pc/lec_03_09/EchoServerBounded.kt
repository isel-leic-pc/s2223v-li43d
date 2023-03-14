package isel.leic.pc.lec_03_09

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class EchoServerBounded(private val port : Int) {
    val MAX_CLIENTS = 2


    private fun sendResponse(writer: PrintWriter, resp : String) {
        writer.println(resp)
        writer.flush()
    }

    fun run() {
        val servSocket = ServerSocket()
        servSocket.bind(InetSocketAddress("0.0.0.0", port))
        while(true) {

            val clientSocket = servSocket.accept()
            logger.info("client ${clientSocket.remoteSocketAddress} connected")

            thread {
                processClient(clientSocket)
            }
        }
    }

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
                sendResponse(writer,line)
            }
            while(true)
        }
        finally {
            sendResponse(writer,"Bye")
            client.close()
            logger.info("client ${client.remoteSocketAddress} disconnected")
        }
    }
}

private fun main() {
    val server = EchoServerBounded(8080)
    server.run()
}