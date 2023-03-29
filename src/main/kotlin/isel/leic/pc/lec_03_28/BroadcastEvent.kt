package isel.leic.pc.lec_03_28

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.Throws
import kotlin.time.Duration

/**
 * A bad implementation of the synchronizer BroadcastEvent
 * that does not guarantee the required semantic of broadcast method,
 * that is resolve all waiters in method "await" after "broadcast" call
 *
 * Note that, due to barging,  a call to "close" method can succeed
 * before the tread blocke in "awwait" reaquires the monitor, and if this is
 * case, that thread will observe the state CLOSED, and will block again
 */
class BroadcastEvent {
    private val monitor = ReentrantLock()
    private val opened = monitor.newCondition()

    private val CLOSED = 0
    private val OPENED = 1

    private var state = CLOSED

    @Throws(InterruptedException::class)
    fun await() {
        monitor.withLock {
            while(state == CLOSED) {
                opened.await()
            }
        }
    }

    fun broadcast() {
        monitor.withLock {
            state = OPENED
            opened.signalAll()
        }
    }

    fun close() {
        monitor.withLock {
            state = CLOSED
        }
    }
}