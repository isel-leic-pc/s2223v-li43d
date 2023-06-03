package isel.leic.pc.lec_05_30.servers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException



suspend fun AsynchronousServerSocketChannel.acceptSuspend()
                : AsynchronousSocketChannel {
    TODO()
}

suspend fun AsynchronousSocketChannel.readSuspend (
                  dst: ByteBuffer) : Int {
    TODO()
}

suspend fun AsynchronousSocketChannel.writeSuspend (
                   dst: ByteBuffer) : Int {
    TODO()
}

