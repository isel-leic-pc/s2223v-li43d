package isel.leic.pc.lec_05_04

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class TwoLockQueue<E> {
    private val putLock = ReentrantLock()
    private val takeLock = ReentrantLock()
    private var head : Node<E>
    private var tail : Node<E>
    init {
        val dummy = Node<E>()
        head = dummy
        tail = dummy
    }
    private class Node<E>( val value : E? = null) {
        @Volatile
        var next : Node<E>? = null
    }
    fun put(elem: E) {
        putLock.withLock {
            val newNode = Node(elem)
            tail.next = newNode
            tail = newNode
        }
    }
    fun take() : E? {
        takeLock.withLock {
            if (head.next == null) return null
            head = head.next!!
            return head.value
        }
    }
}