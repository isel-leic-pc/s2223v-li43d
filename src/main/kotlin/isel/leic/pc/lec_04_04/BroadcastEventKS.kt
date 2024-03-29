package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.Throws
import kotlin.time.Duration

class BroadcastEventKS {
    companion object {
        private val CLOSED = 0
        private val OPENED = 1
    }

    private val monitor = ReentrantLock()
    private val opened = monitor.newCondition()

    private class Waiter {
        var done = false
    }


    private var state = CLOSED
    private val waiters = LinkedList<Waiter>()

    private fun notifyWaiters() {
        waiters.forEach {
            it.done = true
        }
        waiters.clear()
        opened.signalAll()
    }

    @Throws(InterruptedException::class)
    fun await(timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (state == OPENED) return true
            if (timeout.isZero) return false
            // waiting path
            val dueTime = timeout.dueTime()
            val waiter = Waiter()
            waiters.add(waiter)
            do {
                try {
                    opened.await(dueTime)
                    if (waiter.done) return true
                    if (dueTime.isPast) {
                        waiters.remove(waiter)
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    waiters.remove(waiter)
                    throw e
                }
            }
            while(true)
        }
    }

    fun broadcast() {
        monitor.withLock {
            state = OPENED
            notifyWaiters()
        }
    }

    fun close() {
        monitor.withLock {
            state = CLOSED
        }
    }
}

/**
 * In this implementation the BroadcastEvent implementation
 * uses an optimized variant of kernel style, that we named
 * batch notification. Here we don't have the list usually
 * associated to kernel style, but a single object is associated
 * to all blocking operation waiters (that will be notified all at once,
 * receiving exactly the same state - hence the name batch notification)
 */
class BroadcastEventKSBatch {
    companion object {
        private val CLOSED = 0
        private val OPENED = 1
    }

    private val monitor = ReentrantLock()
    private val opened = monitor.newCondition()

    private class CurrentWaiters {
        var done = false
    }

    // the object associated to all current waiters
    // on await operation
    private var currentWaiters = CurrentWaiters()

    private var state = CLOSED

    /**
     * the notification action using batch notification
     * Note that we create a new waiters representant preparing the next batch
     */
    private fun notifyWaiters() {
        currentWaiters.done = true
        opened.signalAll()
        currentWaiters = CurrentWaiters()
    }

    @Throws(InterruptedException::class)
    fun await(timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (state == OPENED) return true
            if (timeout.isZero) return false
            // waiting path
            val dueTime = timeout.dueTime()
            val local = currentWaiters

            do {
                try {
                    opened.await(dueTime)
                    if (local.done) return true
                    if (dueTime.isPast) return false

                }
                catch(e: InterruptedException) {
                    if (local.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    throw e
                }
            }
            while(true)
        }
    }

    fun broadcast() {
        monitor.withLock {
            state = OPENED
            notifyWaiters()
        }
    }

    fun close() {
        monitor.withLock {
            state = CLOSED
        }
    }
}