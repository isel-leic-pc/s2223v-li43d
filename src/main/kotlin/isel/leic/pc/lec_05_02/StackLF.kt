package isel.leic.pc.lec_05_02

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Stack lock-free baseado no algoritmo de Treiber
 */
class StackLF<T> {
    private class Node<T>( val value: T) {
        var next: Node<T>? = null
    }

    private val head = AtomicReference<Node<T>>()

    fun push(value: T) {
        val newNode = Node<T>(value)
        do {
            val obsHead = head.get()
            newNode.next = obsHead
        }
        while(!head.compareAndSet(obsHead, newNode))
    }

    fun pop() : T? {
        do {
            val obsHead = head.get()
            if (obsHead == null) return null
            if (head.compareAndSet(obsHead, obsHead.next)) {
                return obsHead.value
            }

        }
        while(true)
    }

    fun isEmpty() = head.get() == null
}

open class INode {
    var next: INode?  = null
}

class StackLFIL {

    private var head = AtomicReference<INode>()

    fun push(node: INode) {
        do {
            val obsHead = head.get()
            node.next = obsHead
            if (head.compareAndSet(obsHead, node)) {
                return
            }
        }
        while(true);
    }

    fun pop() : INode? {
        do {
            val obsHead = head.get() ?: return null
            if (head.compareAndSet(obsHead, obsHead.next)) {
                return obsHead
            }
        }
        while(true);
    }

    fun isEmpty()  = head.get() == null
}