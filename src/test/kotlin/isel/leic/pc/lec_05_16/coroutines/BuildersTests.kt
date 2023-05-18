package isel.leic.pc.lec_05_16.coroutines

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class BuildersTests {


    @Test
    fun `global_scope_launch_test`() {
        val latch = CountDownLatch(1)
        var sameJob = false
        var job : Job? = null
        job = GlobalScope.launch {
            sameJob = job == coroutineContext.job
            logger.info("before delay")
            delay(3000)
            logger.info("after delay")
            latch.countDown()
        }
        latch.await()
        assertTrue(sameJob)
    }
}