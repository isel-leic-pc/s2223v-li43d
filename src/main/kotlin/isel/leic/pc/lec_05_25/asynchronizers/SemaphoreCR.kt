package isel.leic.pc.lec_05_25.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}


class SemaphoreCR(private var permits: Int = 0 ) {

    private class Waiter(val toAcquire : Int,
                         val cont: CancellableContinuation<Unit>,
                       ) {
        var timer : ScheduledFuture<*>? = null
        var done = false

        fun setTimeoutHandler(timeout : Duration, timeoutHandler : (Waiter)-> Unit) {
            if (timeout != Duration.INFINITE) {
                timer = scheduler.schedule(timeout) {
                    timeoutHandler(this)
                }
            }
        }

    }


    private val mutex = ReentrantLock()
    private val waiters = LinkedList<Waiter>()


    @Throws(TimeoutException::class)
    suspend fun acquire(toAcquire: Int, timeout: Duration): Unit {
        var waiter : Waiter? = null
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                mutex.withLock {
                    if (permits >= toAcquire) {
                        permits -= toAcquire
                        cont.resume(Unit)
                    } else {
                        if (timeout == Duration.ZERO) {
                            cont.resumeWithException(TimeoutException())
                        } else {
                            waiter = Waiter(toAcquire, cont)
                            waiters.add(waiter!!)
                            waiter?.setTimeoutHandler(timeout, ::tryCompleteWithTimeout)

                        }

                    }
                }
            }
        }
        catch(e: CancellationException) {
             waiter?.also {
                 if (tryCompleteWithCancelled(it))
                     throw e
             }

        }


    }

    fun release(toRelease: Int) {
        mutex.withLock {
            permits += toRelease
            while(waiters.size > 0 && waiters.first.toAcquire <= permits) {
                val waiter = waiters.poll()
                permits -= waiter.toAcquire
                waiter.done = true
                waiter.timer?.cancel(true)
                waiter.cont.resume(Unit)
            }
        }
    }

    private fun tryCompleteWithTimeout(waiter : Waiter) {
        mutex.withLock {
            if (!waiter.done) {
                waiter.done = true
                waiters.remove(waiter)
                waiter.cont.resumeWithException(TimeoutException())
            }
        }
    }

    private fun tryCompleteWithCancelled(waiter : Waiter) : Boolean {
        mutex.withLock {
            if (waiter.done) return false
            waiter.done = true
            waiters.remove(waiter)
            return true
        }
    }
}