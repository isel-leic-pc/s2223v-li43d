package isel.leic.pc.lec_04_04

import isel.leic.pc.utils.launchMany
import org.junit.Assert
import org.junit.Test
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




    @Test
    fun `multiple transfers and takers stress test`() {
        val TRANSFER_THREADS = 20
        val TAKER_THREADS = 30
        val lock = ReentrantLock()
        val transferQueue = NAryTransferQueue<Long>()
        val activeTakers = AtomicInteger()
        val activeTransfers = AtomicInteger()
        val nextVal = AtomicLong(1)
        val errors = AtomicInteger()

        val results = mutableSetOf<Long>()
        val testDuration = 30.toDuration(DurationUnit.SECONDS)

        val done = AtomicBoolean()

        val transfers = launchMany(TRANSFER_THREADS, "transfer") {
            while (!done.get()) {
                activeTransfers.incrementAndGet()
                val list = listOf(
                    nextVal.getAndIncrement(),
                    nextVal.getAndIncrement(),
                    nextVal.getAndIncrement(),
                    nextVal.getAndIncrement(),
                    nextVal.getAndIncrement()
                )
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
                lock.withLock {
                    results.add(res ?: 0)
                }
                activeTakers.decrementAndGet()
            }

        }

        Thread.sleep(testDuration.inWholeMilliseconds)
        done.set(true)
        Thread.sleep(500)
        println(
            "Stop test, activerTakers = ${activeTakers.get()}, activeTransfers = ${activeTransfers.get()}")

        while (activeTransfers.get() != 0) {
            println("activeTransfers = ${activeTransfers.get()}")
            thread {
                activeTakers.incrementAndGet()
                val res = transferQueue.take(Duration.INFINITE)
                lock.withLock {
                    results.add(res ?: 0)
                }
                activeTakers.decrementAndGet()
            }

        }

        while (activeTakers.get() != 0) {
            println("activeTakers = ${activeTakers.get()}")
            thread {
                activeTransfers.incrementAndGet()
                if (!transferQueue.transfer(listOf(nextVal.getAndIncrement()), Duration.INFINITE)) {
                    errors.incrementAndGet()
                }
                activeTransfers.decrementAndGet()
            }
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
    }
}