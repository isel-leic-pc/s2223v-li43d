package isel.leic.pc.lec_05_02

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class TwoLockQueue<E> {

    private class Node<E>( val value : E? = null) {
        // safe?
        var next : Node<E>? = null
    }

    private var head : Node<E>
    private var tail : Node<E>

    init {
        val dummy = Node<E>()
        head = dummy
        tail = dummy
    }

    private val putLock = ReentrantLock()
    private val takeLock = ReentrantLock()

    fun put(elem: E) {
        putLock.withLock {
           TODO()
        }
    }

    fun take() : E? {
        takeLock.withLock {
            TODO()
        }
    }
}