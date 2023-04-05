package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.Throws
import kotlin.time.Duration

/**
 * This implementation takes advantage of the fact that
 * no state is really necessary to deliver to waiters on a batch
 * notification, besides from the info that the request succeeds.
 * This could  be achieved with an empty shared object that
 * represents the current waiters. After notification a new shared
 * empty object is created,  and on reentering the monitor, the waiters
 * just check that the object they saved before wait is different from the
 * shared object that exists now!
 *
 * This empty object represents what we call a waiter generation.
 * When it changes inform the waiters that the generation was processed
 * and can proceed
 *
 * This can also be achieved with a primitive value (an int) that changes
 * on each generation, as shown in the implementation below.
 */
class BroadcastEventKSBatch2 {
    private val monitor = ReentrantLock()
    private val opened = monitor.newCondition()

    // tells the current generation number
    private var generation = 0

    private val CLOSED = 0
    private val OPENED = 1

    private var state = CLOSED

    @Throws(InterruptedException::class)
    fun await(timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (state == OPENED) return true
            if (timeout.isZero) return false
            // waiting path
            val dueTime = timeout.dueTime()

            val currgen = generation
            do {
                opened.await(dueTime)
                // if the observed generation is different
                // then the current generation we can get
                // for sure that we can proceed
                if (currgen != generation) return true
                if (dueTime.isPast)  return false
            }
            while(true)
        }
    }

    fun broadcast() {
        monitor.withLock {
            state = OPENED
            // to next generation
            generation++
            opened.signalAll()
        }
    }

    fun close() {
        monitor.withLock {
            state = CLOSED
        }
    }
}