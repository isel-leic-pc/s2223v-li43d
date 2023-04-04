package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.Throws
import kotlin.time.Duration

class BroadcastEventKSBatch2 {
    private val monitor = ReentrantLock()
    private val opened = monitor.newCondition()

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
                if (currgen != generation) return true
                if (dueTime.isPast)  return false
            }
            while(true)
        }
    }

    fun broadcast() {
        monitor.withLock {
            state = OPENED
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