package isel.leic.pc.lec_03_06

import org.junit.Test
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class CounterTests {
    @Test
    fun `increment counter by multile threads test`() {
        val counter = Counter()

        val NTHREADS = 20
        val NITERS = 1000000

        val threads : MutableList<Thread> =
            mutableListOf()

        repeat(NTHREADS) {
            val t = thread {
                repeat(NITERS) {
                    counter.inc()
                }
            }
            threads.add(t)
        }

        threads.forEach {
            it.join()
        }

        assertEquals((NTHREADS*NITERS).toLong(),counter.get() )

    }
}