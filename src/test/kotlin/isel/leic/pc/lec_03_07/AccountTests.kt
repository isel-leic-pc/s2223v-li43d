package isel.leic.pc.lec_03_06

import org.junit.Test
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class AccountTests {

    private fun multipleTransfers(acc1: Account, acc2: Account,
                                  transferOp : (src:Account, dst:Account) -> Unit) {
        val NITERS = 100000

        val thread1 = thread {
            repeat(NITERS) {
                transferOp(acc1, acc2)
                transferOp(acc2, acc1)
            }
        }

        val thread2 = thread {
            repeat(NITERS) {
                transferOp(acc2, acc1)
                transferOp(acc1, acc2)
            }
        }

        thread1.join()
        thread2.join()
    }

    @Test
    fun `multiple thread account access without none synchronization`() {
        val INITIAL_BALANCE = 20000L

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfers(acc1, acc2) {
                src, dst -> src.transfer0(dst, 1000)
        }

        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using shared lock synchronization`() {
        val INITIAL_BALANCE = 20000L

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfers(acc1, acc2) {
                src, dst -> src.transfer1(dst, 1000)
        }

        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using account locks synchronization`() {
        val INITIAL_BALANCE = 20000L

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfers(acc1, acc2) {
                src, dst -> src.transfer2(dst, 1000)
        }
        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using account locks ordered by hashcode`() {
        val INITIAL_BALANCE = 20000L

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfers(acc1, acc2) {
                src, dst -> src.transfer3(dst, 1000)
        }
        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }
}