package isel.leic.pc.lec_04_18.lock_free

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Lazy<T>(private val supplier : () -> T) {
    val mutex = ReentrantLock()



    fun get0() : T {
        mutex.withLock {
            if (value == null) {
                value = supplier()
            }
            return value!!
        }
    }

    @Volatile
    var value : T? = null

    fun get() : T {
        if (value == null) {
            mutex.withLock {
                if (value == null) {
                    value = supplier()
                }
            }
        }
        return value!!
    }
}