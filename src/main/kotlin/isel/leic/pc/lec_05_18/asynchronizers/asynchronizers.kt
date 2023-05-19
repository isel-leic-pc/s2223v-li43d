package isel.leic.pc.lec_05_18.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration


class BroadcastEvent {

    private val opened = false
    private val waiters = LinkedList<Continuation<Unit>>()
    private val mutex = ReentrantLock()

    suspend fun await()  {
         suspendCoroutine<Unit> { cont ->
             mutex.withLock {
                 if (opened)  {
                     cont.resume(Unit)
                 }
                 else {
                     waiters.add(cont)
                 }
             }
         }
    }

    fun broadcast() {
      TODO()
    }

    fun close() {
       TODO()
    }
}