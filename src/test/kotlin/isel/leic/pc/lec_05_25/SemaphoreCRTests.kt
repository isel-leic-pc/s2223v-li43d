package isel.leic.pc.lec_05_25

import isel.leic.pc.lec_05_23.show
import isel.leic.pc.lec_05_25.asynchronizers.SemaphoreCR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}


// tests for Semaphore with acquire suspend
class SemaphoreCRTests {

    @Test
    fun `call acquire with available permits`() {
        val sem = SemaphoreCR(2)

        runBlocking {
            sem.acquire(1, Duration.INFINITE)
            logger.info("after acquire")
        }

    }

    @Test
    fun `call acquire without available permits`() {
        val sem = SemaphoreCR(0)
        val count = AtomicInteger()

        runBlocking  {
            repeat(2) {
                launch {
                    logger.info("before acquire 1")
                    sem.acquire(1, Duration.INFINITE)
                    logger.info("after acquire 1")
                    count.incrementAndGet()

                }
            }

            val releaser = launch {
                delay(1000)
                logger.info("before sem release")
                sem.release(2)
                logger.info("after sem release")
            }
        }

        assertEquals(2, count.get())
    }

    @Test
    fun `call acquire with timeout`() {
        val sem = SemaphoreCR(0)

        runBlocking {
            val acquirer1 = launch {
                logger.info("before acquire 1")
                try {
                    sem.acquire(1, 2.toDuration(DurationUnit.SECONDS))
                    fail()
                }
                catch(e: TimeoutException) {
                    println("timeout exception!")
                    //throw e
                }
                logger.info("after acquire 1")

            }
            acquirer1.join()

            acquirer1.show()
        }

    }
}