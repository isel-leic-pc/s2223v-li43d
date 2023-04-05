package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.launchMany
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NAryTransferQueueTests {
    @Test
    fun `a transfer waiting for necessary takers`() {
        val NTAKERS = 3

        val transferQueue = NAryTransferQueue<Int>()

        var transferRes = false
        val results = IntArray(NTAKERS)

        val transfer = thread {
            transferRes = transferQueue.transfer(listOf(1, 2, 3), Duration.INFINITE)
        }

        Thread.sleep(1000)
        val takers = launchMany(NTAKERS) {
            val res = transferQueue.take(Duration.INFINITE)
            //println("res=$res")
            results[it] = res ?: 0
        }

        takers.forEach {
            it.join()
        }
        transfer.join()

        Assert.assertTrue(transferRes)
        for (v in results) {
            Assert.assertTrue(v >= 1 && v <= 3)
        }
        Assert.assertEquals(3, results.asList().distinct().size)

    }

    @Test
    fun `a transfer resolve waiting takers`() {
        val NTAKERS = 3

        val transferQueue = NAryTransferQueue<Int>()

        var transferRes = false
        val results = IntArray(NTAKERS)

        val takers = launchMany(NTAKERS) {
            val res = transferQueue.take(Duration.INFINITE)
            //println("res=$res")
            results[it] = res ?: 0
        }

        Thread.sleep(1000)

        val transfer = thread {
            transferRes = transferQueue.transfer(listOf(1, 2, 3), Duration.INFINITE)
        }



        takers.forEach {
            it.join()
        }
        transfer.join()

        Assert.assertTrue(transferRes)
        for (v in results) {
            Assert.assertTrue(v >= 1 && v <= 3)
        }
        Assert.assertEquals(3, results.asList().distinct().size)
    }

    private fun listOfLongs(rand: Random, nextVal : AtomicLong) : List<Long> {
        return (1 .. rand.nextInt(10)+1)
            .map {
                nextVal.getAndIncrement()
            }
    }

    /**
     * Here we have a more exhaustive test, that we call a stress test,
     * were there are many threads, executing different operations,
     * many times on a loop, and during a significant time amount.
     * This way, we maximize the probability of problems occurrence, hence
     * the name stress test.
     * On this test we don't test timeout and/or interrupt scenarios.
     *
     * Try to do a stress test for each synchronizer
     */
    @Test
    fun `multiple transfers and takers stress test`() {
        val TRANSFER_THREADS = 20
        val TAKER_THREADS = 30

        // the transfer queue to test
        val transferQueue = NAryTransferQueue<Long>()

        // were to save the values retrieved from transferQueue
        val results = mutableSetOf<Long>()
        // used to get exclusive access to the result set
        val lock = ReentrantLock()

        // to maintain an estimation of active takers and transfers
        val activeTakers = AtomicInteger()
        val activeTransfers = AtomicInteger()

        // every transfer will produce a new value by incrementing this atomic
        val nextVal = AtomicLong(1)

        // account possible errors
        val errors = AtomicInteger()

        // the duration of the test
        val testDuration = 30.toDuration(DurationUnit.SECONDS)

        // to end the loop on each thread using the NAryTransferQueue
        val done = AtomicBoolean()

        val transfers = launchMany(TRANSFER_THREADS, "transfer") {
            while (!done.get()) {
                val random = Random()
                val list = listOfLongs(random, nextVal)
                activeTransfers.incrementAndGet()
                if (!transferQueue.transfer(list, Duration.INFINITE)) {
                    errors.incrementAndGet()
                }
                activeTransfers.decrementAndGet()
            }
        }

        val takers = launchMany(TAKER_THREADS) {
            while(!done.get()) {
                activeTakers.incrementAndGet()
                val res = transferQueue.take(Duration.INFINITE)

                // adding the result
                lock.withLock {
                    results.add(res ?: 0)
                }
                activeTakers.decrementAndGet()
            }

        }

        Thread.sleep(testDuration.inWholeMilliseconds)

        // start ending the test
        done.set(true)

        // give a time and resolve pending transfers and takers
        Thread.sleep(1000)
        println("Stop test, activerTakers = ${activeTakers.get()}, activeTransfers = ${activeTransfers.get()}")

        // first resolve  pending transfers
        while (activeTransfers.get() != 0) {
            //println("activeTransfers = ${activeTransfers.get()}")
            thread {
                activeTakers.incrementAndGet()
                val res = transferQueue.take(Duration.INFINITE)
                lock.withLock {
                    results.add(res ?: 0)
                }
                activeTakers.decrementAndGet()
            }
        }

        // now resolve  last takers
        while (activeTakers.get() != 0) {
            //println("activeTakers = ${activeTakers.get()}")
            thread {
                activeTransfers.incrementAndGet()
                if (!transferQueue.transfer(listOf(nextVal.getAndIncrement()), Duration.INFINITE)) {
                    errors.incrementAndGet()
                }
                activeTransfers.decrementAndGet()
            }
            // we wait some time trying to give enough time for thread execution.
            // But of course this type of synchronization is fragile, and is only accepted in tests
            // that we suppose are done on a controlled environment.
            Thread.sleep(300)
        }

        println("Wait for takers")
        takers.forEach {
            it.join(500)
        }
        println("Wait for transfers")
        transfers.forEach {
            it.join(500)
        }

        transfers.forEach {
            if (it.isAlive)
                println("transfer ${it.name} is alive")
        }

        println("results size = ${results.size}")
        println("nextVal = ${nextVal.get()}")

        // some final assertions
        assertEquals(0, errors.get())
        assertEquals(results.size.toLong(), nextVal.get() -1 )

        assertEquals(0, activeTakers.get())
        assertEquals(0, activeTransfers.get())

    }
}