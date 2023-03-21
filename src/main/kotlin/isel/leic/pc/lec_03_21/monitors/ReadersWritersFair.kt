package isel.leic.pc.lec_03_21.monitors

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Here we have a first scenario were we used "execution delegation" (aka "kernel style")
 * technique in order to provide the required semantics:
 *  - A reader can't proceed if there are waiting writers
 *  - On leaveWriter we  must give  access to all waiting readers
 *    (but not new readers that came after)
 *  - On leaveReader, when there are no active readers, we must give  access
 *    to some waiting writer
 *
 */
class ReadersWritersFair {

    private val monitor = ReentrantLock()
    private val canAccess = monitor.newCondition()

    private var nReaders = 0
    private var writing = false

    // the class used to maintain waiting queues for writers and readers
    // According to the "kernel style", just the state of each node is consulted
    // after waiter reentering the monitor
    // In this case the state is just a flag that is true meaning "can proceed"
    private class Waiter {
        var done = false
    }

    private val waitingReaders = LinkedList<Waiter>()
    private val waitingWriters = LinkedList<Waiter>()

    fun enterReader() {
        monitor.withLock {
            // fast path
            if (!writing ) {
                nReaders++
                return
            }

            // waiting path
            val waiter = Waiter()
            waitingReaders.add(waiter)

            do {
                canAccess.await()
                if (waiter.done) {
                    return
                }
            }
            while(true)
        }
    }

    fun enterWriter() {
        monitor.withLock {
            // fast path
            if (!writing && nReaders == 0) {
                writing = true
                return
            }

            // waiting path
            val waiter = Waiter()
            waitingWriters.add(waiter)

            do {
                canAccess.await()
                if (waiter.done) {
                    return
                }
            }
            while(true)
        }
    }

    fun leaveReader() {
        monitor.withLock {
            nReaders--
            if (nReaders == 0 && waitingWriters.size > 0) {
                // remove from queue and set the synchronization state of the first waiter
                val waiter = waitingWriters.poll()
                waiter.done = true

                // we change the (global) synchronization state here
                writing = true

                // signal all (since we have just one condition variable)
                canAccess.signalAll()
            }
        }
    }

    fun leaveWriter() {
        monitor.withLock {
            writing = false
            if (!waitingReaders.isEmpty()) {
                // process all waiting readers
                waitingReaders.forEach {
                    it.done = true
                }
                // we change the (global) synchronization state here
                nReaders = waitingReaders.size
                waitingReaders.clear()
                canAccess.signalAll()
            }
            else if (waitingWriters.size > 0) {
                // remove from queue and set the synchronization state of the first waiter
                val waiter = waitingWriters.poll()
                waiter.done = true
                // signal all (since we have just one condition variable)

                // we change the (global) synchronization state here
                writing = true
                canAccess.signalAll()
            }
        }
    }
}