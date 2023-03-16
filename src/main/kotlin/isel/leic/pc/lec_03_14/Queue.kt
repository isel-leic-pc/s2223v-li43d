package isel.leic.pc.lec_03_14

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Queue<T>( private val capacity : Int) {
    private val elemsAvaiable = Semaphore(0)
    private val spaceAvaiable = Semaphore(capacity)

    private val mutex = ReentrantLock()

    private val elems = LinkedList<T>()

    fun get() : T {
        elemsAvaiable.acquire()
        var elem : T?
        mutex.withLock {
            elem = elems.pollFirst()
        }
        spaceAvaiable.release()
        // always not null
        return elem!!
    }

    fun put(elem : T) {
        spaceAvaiable.acquire()
        mutex.withLock {
            elems.add(elem)
        }
        elemsAvaiable.release()
    }
}