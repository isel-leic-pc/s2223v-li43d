package isel.leic.pc.lec_05_02

import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicInteger

class UnsafeSpinSemaphore(
    _permits : Int,
    private val maxPermits : Int) {

    val permits = AtomicInteger(_permits)

    init {
        check(_permits >= 0)
    }

    fun acquire(toAcquire : Int) {
        do {
            val obsPermits = permits.get()
            if (obsPermits >= toAcquire &&
                permits.compareAndSet(obsPermits, obsPermits - toAcquire))
                return
            Thread.onSpinWait()
        }
        while(true)

    }

    fun release(toRelease: Int) {
        do {
            val obsPermits = permits.get()
            if (obsPermits + toRelease > maxPermits )
                throw IllegalStateException()
            if (permits.compareAndSet(obsPermits, obsPermits + toRelease))
                return
        }
        while(true)
    }
}
