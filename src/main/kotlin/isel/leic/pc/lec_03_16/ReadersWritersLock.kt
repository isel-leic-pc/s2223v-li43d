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


    fun enterReader() {
        TODO()
    }


    fun leaveReader() {
       TODO()
    }

    fun enterWriter() {
        TODO()
    }

    fun leaveWriter() {
        TODO()
    }
}