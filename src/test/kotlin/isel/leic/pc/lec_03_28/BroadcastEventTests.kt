package isel.leic.pc.lec_03_28

import isel.leic.pc.lec_03_28.utils.launchMany
import mu.KotlinLogging
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

class BroadcastEventTests {

    @Test
    fun `check if open in fact releases all event waiters`()  {
        val NWAITERS = 25

        val event = BroadcastEvent()
        val waitsCompleted = AtomicInteger(0)

        val waiters = launchMany(NWAITERS, "waiter") {
            event.await()
            logger.info("awaked")
            waitsCompleted.incrementAndGet()
        }

        val writer = thread {
            Thread.sleep(1000)
            event.broadcast()

            // just to give an window of opportunity for awaiters
            // if you remove this line the problem is exacerbated.
            TimeUnit.MICROSECONDS.sleep(1)

            event.close()
        }

        waiters.forEach {
            it.join(1500)
        }

        writer.join()

        Assert.assertEquals(NWAITERS, waitsCompleted.get())
    }
}