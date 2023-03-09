package isel.leic.pc.lec_03_06

class Account(private var balance : Long = 0) {
    companion object {
        private val sharedMutex = Any()
    }

    private val mutex = Any()

    fun transfer0(other: Account, toTransfer : Long) : Boolean {
        if (balance < toTransfer) return false
        balance -= toTransfer

        other.balance += toTransfer
        return true
    }

    fun transfer1(other: Account, toTransfer : Long) : Boolean {
        synchronized(sharedMutex) {
            if (balance < toTransfer) return false
            balance -= toTransfer

            other.balance += toTransfer
            return true
        }
    }

    fun transfer2(other: Account, toTransfer : Long) : Boolean {
        synchronized(mutex) {
            if (balance < toTransfer) return false

            synchronized(other.mutex) {
                balance -= toTransfer
                other.balance += toTransfer
            }
            return true
        }
    }

    fun transfer3(other: Account, toTransfer : Long) : Boolean {
        val (mtx1, mtx2) = if (mutex.hashCode() < other.mutex.hashCode())
                                Pair(mutex, other.mutex)
                            else
                                Pair(other.mutex, mutex)

        synchronized(mtx1) {
            if (balance < toTransfer) return false

            synchronized(mtx2) {
                balance -= toTransfer
                other.balance += toTransfer
            }
            return true
        }
    }

    fun getBalance() : Long {
        synchronized(sharedMutex) {
            return balance
        }
    }
}