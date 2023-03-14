package isel.leic.pc.lec_03_09

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class Queue<T>() {
    private val hasElements = Semaphore(0)
    //private val mutex = Any()
    private val mutex = ReentrantLock()

    private val elems = LinkedList<T>()

    fun get() : T {

        hasElements.acquire()
        mutex.withLock {
            return elems.pollFirst()
        }
    }

    fun put(elem : T) {
       mutex.withLock {
           elems.add(elem)
       }
    }
}