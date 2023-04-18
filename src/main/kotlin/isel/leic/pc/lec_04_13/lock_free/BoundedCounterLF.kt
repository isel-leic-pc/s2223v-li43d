package isel.leic.pc.lec_04_13.lock_free

import java.util.concurrent.atomic.AtomicInteger



class BoundedCounterLFBad(val min: Int, val max: Int) {
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

    fun get() = value.get()

}

class BoundedCounterLF(val min: Int, val max: Int) {
    var value = AtomicInteger(min)

    fun inc() : Boolean {
         do {
             val obsValue = value.get()
             if (obsValue == max) return false
         }
         while(!value.compareAndSet(obsValue, obsValue+1))
         return true
    }

    fun dec() : Boolean {
        do {
            val obsValue = value.get()
            if (obsValue == min) return false

            if (value.compareAndSet(obsValue, obsValue-1))
                return true
        }
        while(true)
    }
}