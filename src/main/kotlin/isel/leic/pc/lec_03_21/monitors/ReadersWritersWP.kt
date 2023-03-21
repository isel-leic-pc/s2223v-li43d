package isel.leic.pc.lec_03_21.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ReadersWritersWP {
    private val monitor = ReentrantLock()
    private val canAccess = monitor.newCondition()

    private var nReaders = 0
    private var writing = false
    private var waitingWriters = 0

    fun enterReader() {
        monitor.withLock {
            while(writing || waitingWriters > 0) {
                canAccess.await()
            }
            nReaders++
        }
    }

    fun enterWriter() {
        monitor.withLock {
            while(writing || nReaders > 0) {
                waitingWriters++
                canAccess.await()
                waitingWriters--
            }
            writing = true
        }
    }

    fun leaveReader() {
        monitor.withLock {
            nReaders--
            if (nReaders == 0) {
                canAccess.signalAll()
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