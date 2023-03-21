package isel.leic.pc.lec_03_21.semaphores

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * this class shows an incorrect solution (in many ways) to
 * a readers/writers synchronizer using a (binary) semaphore
 * and a mutex.
 *
 * but it illustrates several ingredients that are
 * common to custom synchronizers:
 *
 * data synchronization - the mutex
 * control synchronization - the semaphore
 * and
 * synchronization state - nReaders and writing private properties
 *
 */
class ReadersWritersBad {
    private val mutex = ReentrantLock()  // support for exclusive access

    // here we use the semaphore has an hint that the resource can be accessed
    private val canAccess = Semaphore(0)

    // synchronization state
    private var nReaders = 0        // the number of active writers
    private var writing = false     // a writer is active

    fun enterReader() {
        mutex.lock()

        while (nReaders == 0 && writing) {
            mutex.unlock()
            // problem! anything can happen between unlock and acquire.
            // Suppose that, before we acquire the semaphore, the writer invoke leaveWriter, and
            // another reader enter at this point acquiring the semaphore first.
            // Now we remain blocked in the semaphore, although we can proceed too!
            // There is no way to avoid this vulnerability window...
            canAccess.acquire()

            // The synchronization state can be changed
            // from the one that exist at canAccess release time.
            // We must reevaluate this state after reentering the mutex
            mutex.lock()
        }
        nReaders++
        mutex.unlock()
    }

    fun enterWriter() {
        mutex.lock()
        while (writing || nReaders > 0) {
            mutex.unlock()
            // problem! anything can happen between unlock and acquire
            canAccess.acquire()

            // The synchronization state can be changed
            // from the one that exist at canAccess release time.
            // We must revaluate this state after reentering the mutex
            mutex.lock()
        }
        writing = true
        mutex.unlock()
    }

    fun leaveWriter() {
        mutex.withLock {
            writing = false
            canAccess.release()
        }
    }


    fun leaveReader() {
        mutex.withLock {
            nReaders--
            if (nReaders == 0)
                canAccess.release()
        }
    }
}

/**
 * The previous (pseudo) solution tries to use a pattern
 * similar to that we will use with monitors, but can't be done correctly,
 * due to the lack of atomicity between the mutex unlock and the semaphore acquire,
 * just to mention the most prominent problem.
 * This new version works, but it is tricky.
 * In addition, it gives priority to readers in the sense
 * that while there are active readers, no writer can enter.
 * This is bad, since we normally want exactly the opposite.
 * And the opposite is very difficult to achieve.
 * We need a better construct. And we have it.
 * Let's go use Monitors!
 */
class ReadersWriters {
    private val mutex = ReentrantLock()

    // now the semaphore is used to directly provide access to the data protected
    // by the synchronizer
    private val canAccess = Semaphore(1)

    private var nReaders = 0

    fun enterReader() {
        // this is very tricky
        // we don't release mutex while waiting for semaphore
        // In general we don't want to do this, since we risk deadlock situations
        // but in this case is benign, because forward readers can't proceed anyway before
        // we acquire the semaphore
        mutex.withLock {
            if (nReaders == 0) {
                canAccess.acquire()
            }
            nReaders++
        }
    }

    fun leaveReader() {
        mutex.withLock {
            if (--nReaders == 0) {
                canAccess.release()
            }
        }
    }

    fun enterWriter() {
        canAccess.acquire()
    }

    fun leaveWriter() {
        canAccess.release()
    }
}