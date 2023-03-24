package isel.leic.pc.lec_03_23.monitors

import isel.leic.pc.utils.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SimpleSemaphore(private var permits : Int = 0) {
    private val monitor = ReentrantLock()
    private val hasPermits = monitor.newCondition()

    @Throws(InterruptedException::class)
    fun acquire(timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (permits > 0) {
                permits--
                return true
            }
            if (timeout.isZero) {
                return false
            }
            // waiting path
            val dueTime = timeout.dueTime()
            do {
                try {
                    hasPermits.await(dueTime)
                    if (permits > 0) {
                        permits--
                        return true
                    }
                    if (dueTime.isPast) return false
                }
                catch(e: InterruptedException) {
                    if (permits > 0) {
                        hasPermits.signal()
                    }
                    throw e
                }
            }
            while(true)
        }
    }

    /**
     * Just to show timeout management
     * with alternative using relative time
     */
    @Throws(InterruptedException::class)
    fun acquire2(timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (permits > 0) {
                permits--
                return true
            }
            if (timeout.isZero) {
                return false
            }
            // waiting path
            val tmWrapper = MutableTimeout(timeout)
            do {
                try {
                    hasPermits.await(tmWrapper)
                    if (permits > 0) {
                        permits--
                        return true
                    }
                    if (tmWrapper.elapsed) return false
                }
                catch(e: InterruptedException) {
                    if (permits > 0) {
                        hasPermits.signal()
                    }
                    throw e
                }
            }
            while(true)
        }
    }
}