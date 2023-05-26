package isel.leic.pc.coroutines.servers

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

private object acceptCompleted : CompletionHandler<AsynchronousSocketChannel,
                                            Continuation<AsynchronousSocketChannel>> {
    override fun completed(result: AsynchronousSocketChannel,
                           cont:  Continuation<AsynchronousSocketChannel>) {

       cont.resume(result)

    }

    override fun failed(exc: Throwable,
                        cont:  Continuation<AsynchronousSocketChannel>) {
        cont.resumeWithException(exc)
    }
}

private object rwCompleted : CompletionHandler<Int, Continuation<Int>> {
    override fun completed(result: Int, cont: Continuation<Int>) {
        cont.resume(result)
    }

    override fun failed(exc: Throwable, cont: Continuation<Int>) {
        cont.resumeWithException(exc)
    }
}


suspend fun AsynchronousServerSocketChannel.acceptSuspend(timeout: Long = Long.MAX_VALUE )
                : AsynchronousSocketChannel {
    val channel = this
    if (timeout == Long.MAX_VALUE) {
        return suspendCancellableCoroutine<AsynchronousSocketChannel> { continuation ->
            continuation.invokeOnCancellation {
                close()
            }
            channel.accept(continuation, acceptCompleted)
        }
    }
    return withTimeout(timeout) {
        suspendCancellableCoroutine<AsynchronousSocketChannel> { continuation ->
            continuation.invokeOnCancellation {
                close()
            }
            channel.accept(continuation, acceptCompleted)
        }


    }
}

suspend fun AsynchronousSocketChannel.readSuspend (
                  dst: ByteBuffer,
                  timeout: Long = Long.MAX_VALUE) : Int {
    val channel = this
    if (timeout == Long.MAX_VALUE) {
        return suspendCancellableCoroutine<Int> { continuation ->
            channel.read(dst, continuation, rwCompleted)
        }
    }
    else {
        return suspendCancellableCoroutine<Int> { continuation ->
            channel.read(dst, timeout, TimeUnit.MILLISECONDS, continuation, rwCompleted)
        }
    }
}

suspend fun AsynchronousSocketChannel.writeSuspend (
                   dst: ByteBuffer,
                   timeout: Long = Long.MAX_VALUE) : Int {
    val channel = this
    if (timeout == Long.MAX_VALUE) {
        return suspendCancellableCoroutine<Int> { continuation ->
            channel.write(dst, continuation, rwCompleted)
        }
    }
    else {
        return suspendCancellableCoroutine<Int> { continuation ->
            channel.write(dst, timeout, TimeUnit.MILLISECONDS, continuation, rwCompleted)
        }
    }
}
