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


    private fun start() {
        this.thread = thread(start = false) {
            try {
                set(callable.call())
            }
            catch( e: Exception) {
                setError(e)
            }
        }
        this.thread?.start()
    }

    private fun tryGetResult() : V? {
        if (state == State.COMPLETED) return value!!
        if (state === State.ERROR) throw ExecutionException(error)
        if (state == State.CANCELLED)  CancellationException()
        return null
    }

    @Throws(InterruptedException::class)
    private fun get(timeout : Duration) : V {
        monitor.withLock {
            // fast path
            val res = tryGetResult()
            if (res != null) return res
            if (timeout.isZero) throw TimeoutException()

            val dueTime = timeout.dueTime()
            // wait path
            do {
                done.await(dueTime)
                val res = tryGetResult()
                if (res != null) return res
                if (dueTime.isPast) throw TimeoutException()
            }
            while(true)
        }
    }


    // interface Future

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        monitor.withLock {
            if (state != State.ACTIVE) return false
            state = State.CANCELLED

            if (mayInterruptIfRunning)
                thread?.interrupt()
            return true
        }
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

    @Throws(InterruptedException::class)
    override fun get(timeout: Long, unit: TimeUnit): V {
        return get(unit.toMillis(timeout).toDuration(DurationUnit.MILLISECONDS))
    }

    @Throws(InterruptedException::class)
    override fun get(): V {
        return get(Duration.INFINITE)
    }

    // completion methods

    private fun set(value : V) {
        monitor.withLock {
           if (state == State.ACTIVE) {
               this.value = value
               state = State.COMPLETED
               done.signalAll()
           }
        }
    }

    private fun setError(e : Exception) {
        monitor.withLock {
            if (state == State.ACTIVE) {
                this.error = e
                state = State.ERROR
                done.signalAll()
            }
        }
    }

}