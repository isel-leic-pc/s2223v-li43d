package isel.leic.pc.lec_03_28

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * This implementation of a counter semaphore (with arbitrary units
 * for acquire and release operations) is unfair for acquisitions of
 * a large number of units, since acquisitions of a small number of units
 * have a larger probability of success.
 */
class CounterSemaphore(private var permits: Int) {

    private val monitor = ReentrantLock()

    private val avaiablePermits : Condition =
        monitor.newCondition()

    @Throws(InterruptedException::class)
    fun acquire(units: Int) {
        monitor.withLock {
            while(units > permits) {
                avaiablePermits.await()
            }
            permits -= units
        }
    }

    fun release(units: Int) {
       monitor.withLock {
           permits += units;
           avaiablePermits.signalAll()
       }
    }
}

/**
 * This implementation of a counter semaphore uses kernel style technique
 * in order to guarantee fifo acquisition of units (the first acquirer,
 * independently of the number of units acquired, is processed before
 * the other acquirers).
 */
class SemaphoreFifo(private var permits: Int) {
    private val monitor = ReentrantLock()

    private val avaiablePermits : Condition =
        monitor.newCondition()

    private class PendingAcquire(val units: Int) {
        var acquired = false
    }

    private val pendingAcquires = LinkedList<PendingAcquire>()


    private fun notifyWaiters() {
        var awaken = false
        while(pendingAcquires.size > 0 &&
              pendingAcquires.first().units <= permits ) {
            val pa = pendingAcquires.poll()
            permits -= pa.units
            pa.acquired = true
            awaken = true
        }
        if (awaken) avaiablePermits.signalAll()
    }


    @Throws(InterruptedException::class)
    fun acquire(units: Int, timeout : Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (pendingAcquires.isEmpty() && permits >= units) {
                permits -= units
                return true
            }
            if (timeout.isZero) return false

            // waiting path
            val pendingAcquire = PendingAcquire(units)
            pendingAcquires.add(pendingAcquire)

            val dueTime = timeout.dueTime()
            do {
                try {
                    avaiablePermits.await(dueTime)
                    //println("acquirer with ${units} awaken")
                    if (pendingAcquire.acquired) return true
                    if (dueTime.isPast) {
                        pendingAcquires.remove(pendingAcquire)
                        notifyWaiters()
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (pendingAcquire.acquired) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingAcquires.remove(pendingAcquire)
                    notifyWaiters()
                }
            }
            while(true)

        }
    }

    fun release(units: Int) {
        monitor.withLock {
            permits += units
            notifyWaiters()
        }
    }
}

/**
 *  This implementation of a counter semaphore also uses kernel style technique
 *  in order to guarantee fifo acquisition of units (the first acquirer,
 *  independently of the number of units acquired, is processed before
 *  the other acquirers).
 *  And additionally uses specific notification optimization, in order to
 *  avoid awake an acquirer that will be not served.
 *  This is achieved associating a different condition for each acquirer
 */
class SemaphoreFifoSN(private var permits: Int) {
    private val monitor = ReentrantLock()

    private class PendingAcquire(val units: Int, val cond:Condition) {
        var acquired = false
    }

    private val pendingAcquires = LinkedList<PendingAcquire>()

    private fun notifyWaiters() {
        while(pendingAcquires.size > 0 &&
            pendingAcquires.first().units <= permits ) {
            val pa = pendingAcquires.poll()
            //println("notify acquirer with ${pa.units}")

            permits -= pa.units
            pa.acquired = true

            // notify the specific condition
            pa.cond.signal()
        }
    }

    @Throws(InterruptedException::class)
    fun acquire(units: Int, timeout : Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (pendingAcquires.isEmpty() && permits >= units) {
                permits -= units
                return true
            }
            if (timeout.isZero) return false

            // waiting path
            val pendingAcquire =
                PendingAcquire(units, monitor.newCondition())
            pendingAcquires.add(pendingAcquire)

            val dueTime = timeout.dueTime()
            do {
                try {
                    // waiting on the specific condition
                    pendingAcquire.cond.await(dueTime)
                    //println("acquirer with ${units} awaken")
                    if (pendingAcquire.acquired) return true
                    if (dueTime.isPast) {
                        pendingAcquires.remove(pendingAcquire)
                        notifyWaiters()
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (pendingAcquire.acquired) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingAcquires.remove(pendingAcquire)
                    notifyWaiters()
                }
            }
            while(true)

        }
    }

    fun release(units: Int) {
        monitor.withLock {
            permits += units
            notifyWaiters()
        }
    }
}
