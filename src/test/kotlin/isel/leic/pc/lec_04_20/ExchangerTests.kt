package isel.leic.pc.lec_04_20

import isel.leic.pc.utils.launchMany
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExchangerTests {

    /**
     * auxiliary method to test both exchanger implementations
     */
    private  fun multiple_exchanges(exchanger : IExchanger<Int>) {
        val NEXCHANGES = 10000
        val EXCHANGERS = 4

        val valGenerator = AtomicInteger(1)
        val mutex = ReentrantLock()
        val result = mutableSetOf<Int>()

        val threads = launchMany(EXCHANGERS, tName = "exchanger") {
            repeat(NEXCHANGES) {
                val t = exchanger.exchange(valGenerator.getAndIncrement())
                mutex.withLock {
                    result.add(t)
                }
            }
        }

        for(t in threads) {
            t.join(5000)
        }
        println("Result size = ${result.size}")


        for(t in threads) {
            println("thread ${t.name}: isAlive=${t.isAlive}")
            // note that an exchanger of the initial set can stay alone
            // without completing the expected exchanges
            // this cpde is just to resolve this scenario
            if (t.isAlive) {
                thread {
                    repeat((EXCHANGERS*NEXCHANGES - result.size)/2) {
                        val t = exchanger.exchange(valGenerator.getAndIncrement())
                        mutex.withLock {
                            result.add(t)
                        }
                    }
                }.join()
                break
            }
        }

        assertEquals(EXCHANGERS*NEXCHANGES, result.size)
        for(v in result) {
            assertTrue { v >= 1 && v <= EXCHANGERS*NEXCHANGES }
        }
    }

    @Test
    fun `multiple exchanges test with exchanger monitor`() {
        multiple_exchanges(Exchanger<Int>())
    }

    @Test
    fun `multiple exchanges test with exchanger lock-free`() {
        multiple_exchanges(ExchangerLF<Int>())
    }
}