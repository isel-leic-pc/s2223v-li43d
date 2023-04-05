package isel.leic.pc.lec_04_11

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SimpleFuture<V>(val callable : Callable<V>) : Future<V> {
    companion object {
        fun <V> execute(callable : Callable<V>) : Future<V> {
            val fut = SimpleFuture(callable)
            fut.start()
            return fut
        }
    }

    private val monitor = ReentrantLock()
    private val done = monitor.newCondition()

    private var thread : Thread? = null
    private var value : V? = null
    private var error : Exception? = null

    private enum class State { ACTIVE, COMPLETED, CANCELLED, ERROR }

    private var state = State.ACTIVE

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        monitor.withLock {
           TODO()
        }
    }

    private fun set(value : V) {
        monitor.withLock {
            TODO()
        }
    }

    private fun setError(e : Exception) {
        monitor.withLock {
            TODO()
        }
    }

    private fun start() {
         TODO()
    }

    override fun isCancelled(): Boolean {
        monitor.withLock {
            return state == State.CANCELLED
        }
    }

    override fun isDone(): Boolean {
        monitor.withLock {
            return state != State.ACTIVE
        }
    }

    private fun get(timeout : Duration) : V {
        monitor.withLock {
           TODO()
        }
    }

    override fun get(timeout: Long, unit: TimeUnit): V {
        return get(unit.toMillis(timeout).toDuration(DurationUnit.MILLISECONDS))
    }

    override fun get(): V {
        return get(Duration.INFINITE)
    }

}