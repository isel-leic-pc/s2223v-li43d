package isel.leic.pc.lec_04_20

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Interface useful for common code tests on
 * both implementations (monitor and lock-free)
 */
interface IExchanger<V> {
    fun exchange(v: V) : V
}

/**
 * An implementation of Exchanger using a monitor
 * We must use kernel style to guarantee that an exchanged
 * is completed on second partner arrival
 */
class Exchanger<V> : IExchanger<V> {

    /**
     * instances of this class represent an active exchange
     */
    private class Exchange<V>(val value : V) {
        var other : V? = null
    }

    // the usual stuff
    private val monitor = ReentrantLock()
    private val exchangeDone = monitor.newCondition()

    // the active (current) exchange, if not null
    private var exch : Exchange<V>? = null

    /**
     * just an auxiliary method to complete the active exchange
     * it assumes running with monitor lock acquired
     * and with an active exchange in progress
     * Note what means complete an exchange:
     * 1- deliver the completing value (  exch!!.other = other)
     * 2- signal the waiting partner that the value is already present
     * 3- put the exchanger in an inactive state ( exch = null)
     *
     * On kernel style all these steps are responsibility of the second partner
     * (the notifier one)
     */
    private fun complete(other: V) : V {
        exch!!.other = other
        exchangeDone.signal()
        return exch!!.value.also { exch = null }
    }

    @Throws(InterruptedException::class)
    override fun exchange(v: V) : V {
         monitor.withLock {
             // fast path
             if (exch != null) {
                 return complete(v)
             }
             // waiting path
             val my_exch = Exchange(v)
             exch = my_exch
             do {
                try {
                    exchangeDone.await()
                    // note, we just check our local state! (my_exch)
                    if (my_exch.other != null) return my_exch.other!!
                }
                catch(e: InterruptedException) {
                    if (my_exch.other != null) {
                        Thread.currentThread().interrupt()
                        return my_exch.other!!
                    }
                    exch = null
                    throw e
                }
             }
             while(true)
         }
    }
}

/**
 * A lock-free Exchanger implementation
 * In this implementation we don't support cancellation
 * (by interruption of the thread waiting for exchange partner)
 * This is left as an exercise
 */
class ExchangerLF<V> : IExchanger<V> {

    // the veru same auxiliary class of the monitor with kernel style implementation
    private class Exchange<V>(val value : V) {
        var other : V? = null
    }

    // the current exchange wrapped by an AtomicReference
    // in order to have CAS support
    private var exch =
            AtomicReference<Exchange<V>>()

    override fun exchange(v: V) : V {
        // this is done in eager form just to guarantee that we build only one Exchange.
        // We could also do it in a lazy way, what is left as an exercise
        val my_exch = Exchange(v)
        do {
           val obsExchange = exch.get()
           if (obsExchange == null) {
                // no exchange is active, we must create one
                // a CAS is necessary to resolve races in this creation
                if (exch.compareAndSet(null, my_exch)) {
                    // this is the waiting part for the partner
                    // timeout and cancellation (interruption) options are missing
                    // Try to support cancellation
                    while(my_exch.other == null)
                        Thread.yield()
                    return my_exch.other!!
                }
           }
            else {
                // an exchange is active, just complete it
                if (exch.compareAndSet(obsExchange, null)) {
                    obsExchange.other = v
                    return obsExchange.value
                }
           }
        }
        while(true)
    }
}