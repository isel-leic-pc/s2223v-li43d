package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class NAryTransferQueue<E> {
    val monitor = ReentrantLock()

    private class PendingTransfer<T>(val msgs : List<T>, val cond: Condition) {
        var done = false
    }

    private class PendingTake<T>(val cond: Condition) {
        var msg: T? = null
    }

    private val pendingTransfers = LinkedList<PendingTransfer<E>>()
    private val pendingTakers = LinkedList<PendingTake<E>>()

    private fun sendToTakers(msgs : List<E>) {
        for(m in msgs) {
            with(pendingTakers.poll()) {
                msg = m
                cond.signal()
            }
        }
    }

    @Throws(InterruptedException::class)
    fun transfer(messages : List<E>, timeout : Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (pendingTransfers.isEmpty() && pendingTakers.size >= messages.size) {
                //println("transfer resolve")
                sendToTakers(messages)
                return true
            }
            if (timeout.isZero) return false
            val dueTime = timeout.dueTime()
            val pTransfer = PendingTransfer<E>(messages, monitor.newCondition())
            pendingTransfers.add(pTransfer)
            do {
                try {
                    //println("transfer waiting")
                    pTransfer.cond.await(dueTime)
                    //println("transfer awaken")
                    if (pTransfer.done) return true
                    if (dueTime.isPast) {
                        pendingTransfers.remove(pTransfer)
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (pTransfer.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingTransfers.remove(pTransfer)
                    throw e
                }
            }
            while(true)
        }
    }

    @Throws(InterruptedException::class)
    fun take(timeout : Duration) : E? {
        monitor.withLock {
            // fast path
            if (pendingTransfers.size > 0 &&
                pendingTransfers.first().msgs.size == pendingTakers.size + 1) {
                //println("rendez-vous!")
                val transfer = pendingTransfers.poll()
                val msgs = LinkedList<E>(transfer.msgs)
                val myMsg = msgs.pollLast()
                sendToTakers(msgs)
                transfer.done = true
                transfer.cond.signal()
                return myMsg
            }
            if (timeout.isZero) return null
            // waiting path
            val dueTime = timeout.dueTime()
            val pTaker = PendingTake<E>(monitor.newCondition())
            pendingTakers.add(pTaker)
            do {
                try {
                    //println("taker waiting")
                    pTaker.cond.await(dueTime)
                    //println("taker awaken")
                    if (pTaker.msg != null) return pTaker.msg
                    if (dueTime.isPast) {
                        pendingTakers.remove(pTaker)
                        return null
                    }
                }
                catch(e: InterruptedException) {
                    if (pTaker.msg != null) {
                        Thread.currentThread().interrupt()
                        return pTaker.msg
                    }
                    pendingTakers.remove(pTaker)
                    throw e
                }
            }
            while(true)
        }
    }
}