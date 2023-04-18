package isel.leic.pc.lec_04_13.lock_free

import java.util.concurrent.atomic.AtomicInteger

class SpinLock {

    var value = AtomicInteger(1)

    fun enter() {
        while(!value.compareAndSet(1, 0))
            Thread.onSpinWait()
    }


    fun leave() {
        value.set(1)
    }
}