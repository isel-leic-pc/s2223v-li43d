package isel.leic.pc.lec_05_16

import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

typealias AcceptContinuation = (Throwable?, AsynchronousSocketChannel?) -> Unit
typealias RWContinuation = (Throwable?, Int) -> Unit



private val logger = KotlinLogging.logger {}

object acceptCompleted : CompletionHandler<AsynchronousSocketChannel, AcceptContinuation> {
    override fun completed(result: AsynchronousSocketChannel, cont: AcceptContinuation) {
        cont(null, result)
    }

    override fun failed(exc: Throwable, cont: AcceptContinuation) {
        cont(exc, null)
    }
}

object rwCompleted : CompletionHandler<Int, RWContinuation> {
    override fun completed(result: Int, cont: RWContinuation) {
       cont(null, result)
    }

    override fun failed(exc: Throwable, cont: RWContinuation) {
        cont(exc, -2)
    }
}


object acceptCompletedCF : CompletionHandler<AsynchronousSocketChannel,
                           CompletableFuture<AsynchronousSocketChannel>> {

    override fun completed(result: AsynchronousSocketChannel,
                           cf: CompletableFuture<AsynchronousSocketChannel>) {
        cf.complete(result)
    }

    override fun failed(exc: Throwable,cf: CompletableFuture<AsynchronousSocketChannel>) {
        cf.completeExceptionally(exc)
    }
}

fun AsynchronousServerSocketChannel.accept(cont: AcceptContinuation) {
    this.accept(cont,acceptCompleted )
}

fun AsynchronousServerSocketChannel.acceptAsync() : CompletableFuture<AsynchronousSocketChannel> {
    val acceptCF = CompletableFuture<AsynchronousSocketChannel>()

    this.accept(acceptCF, acceptCompletedCF)

    return acceptCF
}

fun AsynchronousSocketChannel.read (dst: ByteBuffer, cont: RWContinuation) {
   this.read(dst, cont, rwCompleted)
}

fun AsynchronousSocketChannel.readTimeout (dst: ByteBuffer, tmMillis : Long, cont: RWContinuation) {
    this.read(dst, tmMillis, TimeUnit.MILLISECONDS, cont, rwCompleted )
}

fun AsynchronousSocketChannel.write (src: ByteBuffer, cont: RWContinuation) {
  this.write(src, cont, rwCompleted)
}

fun closeConnection(connectionChannel : AsynchronousSocketChannel) {
    logger.info("client ${connectionChannel.remoteAddress} disconnected")
    connectionChannel.close()
}






