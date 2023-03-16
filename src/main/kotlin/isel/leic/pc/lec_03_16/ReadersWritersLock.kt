package isel.leic.pc.lec_03_16

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * An implementation for a lock
 * with readers/writers semantic and readers priority
 * using semaphores
 * Unfortunately a version with writers priority
 * is much more difficult
 *
 * The monitor concept will permit building
 * readers/writers lock with different semantics with
 * the same difficulty level
 */
class ReadersWritersLock {
    private var nReaders = 0
    private val dataAccess = Semaphore(1)
    private val mutex = ReentrantLock()

    fun enterReader() {
        mutex.withLock {
            ++nReaders
            if (nReaders == 1)
                dataAccess.acquire()
        }
    }

    fun leaveReader() {
        mutex.withLock {
            --nReaders
            if (nReaders == 0)
                dataAccess.release()
        }
    }

    fun enterWriter() {
        dataAccess.acquire()
    }

    fun leaveWriter() {
        dataAccess.release()
    }
}