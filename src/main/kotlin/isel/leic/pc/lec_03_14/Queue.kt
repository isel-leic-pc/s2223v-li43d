package isel.leic.pc.lec_03_14

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Queue<T> {
    private val elemsAvaiable = Semaphore(0)

    private val mutex = ReentrantLock()

    private val elems = LinkedList<T>()

    fun get() : T {
        elemsAvaiable.acquire()
        mutex.withLock {
            return elems.pollFirst()
        }
    }

    fun put(elem : T) {
        mutex.withLock {
            elems.add(elem)
        }
        elemsAvaiable.release()
    }
}