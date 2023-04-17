package isel.leic.pc.lec_04_13.lock_free

import java.util.concurrent.atomic.AtomicInteger


class BoundedCounterLF(val min: Int, val max: Int) {
    var value = AtomicInteger(min)

    fun inc() : Boolean {
        if (value.get() == max) return false
        value.incrementAndGet()
        return true
    }

    fun dec() : Boolean {
        if (value.get() == min) return false
        value.decrementAndGet()
        return true
    }
}