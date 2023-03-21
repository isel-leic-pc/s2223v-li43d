package isel.leic.pc.lec_03_21.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ReadersWritersRP {

    private val monitor = ReentrantLock()
    private val canAccess = monitor.newCondition()

    private var nReaders = 0
    private var writing = false

    fun enterReader() {
        monitor.withLock {
            while(writing) {
                canAccess.await()
            }
            nReaders++
        }
    }

    fun enterWriter() {
        monitor.withLock {
            while(writing || nReaders > 0) {
                canAccess.await()
            }
            writing = true
        }
    }

    fun leaveReader() {
        monitor.withLock {
            nReaders--
            if (nReaders == 0) {
                canAccess.signal()
            }
        }
    }

    fun leaveWriter() {
        monitor.withLock {
            writing = false
            canAccess.signalAll()
        }
    }
}