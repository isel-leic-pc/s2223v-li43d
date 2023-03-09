package isel.leic.pc.lec_03_06

class Counter(private var value: Long = 0) {
    private val mutex = Any()

    fun inc() {
        synchronized(mutex) {
            value++
        }
    }

    fun get() : Long {
        synchronized(mutex) {
            return value
        }
    }
}