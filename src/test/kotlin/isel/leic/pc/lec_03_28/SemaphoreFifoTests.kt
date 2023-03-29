package isel.leic.pc.lec_03_28

import mu.KotlinLogging
import org.junit.Test
import org.junit.Assert.*
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

class SemaphoreFifoTests {

    @Test
    fun `simple timeout evaluation`() {
        val sem = SemaphoreFifo(0)
        var timeout = false
        val t = thread {
            if (sem.acquire(1,
                2000.toDuration(DurationUnit.MILLISECONDS)) == false) {
                timeout = true
            }
        }

        t.join(4000)
        assertTrue(timeout)
    }

    @Test
    fun `check FIFO order on a multiple acquirer single release scenario`() {
        val sem = SemaphoreFifo(0)
        val NACQUIRERS = 10

        val results = IntArray(NACQUIRERS)
        val currIdx = AtomicInteger()

        val acquirers = (0 until NACQUIRERS)
                        .map {
                            var t = thread  {
                                //logger.info("acquirer started")
                                if (sem.acquire(NACQUIRERS - it, Duration.INFINITE)) {
                                    val localCurr = currIdx.getAndIncrement()
                                    results[localCurr] = it
                                }
                                //logger.info("units acquired")
                            }
                            sleep(50)
                            t
                        }

        val releaser = thread {
            repeat(NACQUIRERS*(NACQUIRERS+1)/2) {
                //logger.info("release one")
                sem.release(1)
                sleep(20)
            }
        }

        releaser.join()
        acquirers.forEach {
            it.join()
        }

        repeat(NACQUIRERS) {
            assertEquals(it, results[it] )
        }

    }



}