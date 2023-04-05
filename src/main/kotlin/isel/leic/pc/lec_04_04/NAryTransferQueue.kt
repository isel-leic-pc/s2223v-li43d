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

/**
 * Este sincronizador é uma possível implementação do exercício proposto
 * na lista de exercícios da aula de 28/3
 * O método "transfer" tem a capacidade de entregar múltiplas mensagens à fila,
 * com a garantia de atomicidade nessa entrega: ou todas as mensagens são entregues
 * ou nenhuma é entregue. O método "transfer" é potencialmente bloqueante, retornando true
 * apenas quando todas as mensagens entregues tenham sido retiradas via o método take.
 * O método "transfer" pode também retornar false, no caso do tempo de espera ter expirado,
 * ou acabar com o lançamento da excepção InterruptedException em caso de interrupção.
 * Nestes dois casos, expiração de tempo e interrupção, deve ser garantido que nenhuma
 * mensagem foi removida por um "take".O método "take" é potencialmente bloqueante,
 * retornando a mensagem removida, ou null caso não seja possível remover uma mensagem
 * dentro do tempo definido. O método "take" é também sensível a interrupções.
 * O sincronizador deve usar um critério FIFO (first in first out) para a finalização
 * com sucesso das operações "transfer" e "take". Por exemplo, uma chamada a transfer
 * só deve ser concluída com sucesso quando todas as chamadas a "transfer" anteriores
 * tenham sido concluídas.
 */
class NAryTransferQueue<E> {
    val monitor = ReentrantLock()

    /**
     * We use kernel style on this implementation which gives in generally more simple
     * solutions. In this case we need FIFO discipline on transfer operations and
     * deliver atomicity to pending takers. Those requirements are not easily done
     * without kernel style (execution delegation).
     */

    /**
     * instances of this class represents threads waiting for a transfer
     * completion. Note the use of specific notification (every pending transfer
     * has a specific condition)
     */
    private class PendingTransfer<T>(val msgs : List<T>, val cond: Condition) {
        var done = false
    }

    /**
     * instances of this class represents threads waiting for a take
     * completion. Note the use of specific notification (every pending take
     * has a specific condition)
     */
    private class PendingTake<T>(val cond: Condition) {
        var msg: T? = null
    }

    private val pendingTransfers = LinkedList<PendingTransfer<E>>()
    private val pendingTakers = LinkedList<PendingTake<E>>()

    /**
     * auxiliary method to atomically deliver the messages of a transfer
     * operation to a set of pending takers.
     * Note this assumed to run owning the monitor mutex.
     */
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
            // in order to do a fast path exit,
            // is required that no pending transfer exists (FIFO order)
            // and there are the necessary takers (atomic delivery)
            if (pendingTransfers.isEmpty() && pendingTakers.size >= messages.size) {
                sendToTakers(messages)
                return true
            }
            if (timeout.isZero) return false
            val dueTime = timeout.dueTime()
            val pTransfer = PendingTransfer<E>(messages, monitor.newCondition())
            pendingTransfers.add(pTransfer)
            do {
                try {
                    pTransfer.cond.await(dueTime)
                    // on kernel style is required that only local state (of the method)
                    // are observed, and not monitor state, unless it is guaranteed to
                    // be an immutable terminal state
                    if (pTransfer.done) return true
                    if (dueTime.isPast) {
                        pendingTransfers.remove(pTransfer)
                        // here something should have been done
                        // Can you find what was forgotten?
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (pTransfer.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingTransfers.remove(pTransfer)
                    // here something should have been done
                    // Can you find what was forgotten?
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
            // Here, the taker check if he can resolve a pending transfer
            // using already pending takers
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