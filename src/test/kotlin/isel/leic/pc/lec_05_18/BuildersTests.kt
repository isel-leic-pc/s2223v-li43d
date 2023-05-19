package isel.leic.pc.lec_05_18

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

class BuildersTests {

    @Test
    fun `global scope launch test`() {
        val latch = CountDownLatch(1)

        GlobalScope.launch {
            logger.info("before delay")
            delay(3000)
            logger.info("after delay")
            latch.countDown()
        }
        latch.await()
        logger.info("done!")
    }

    @Test
    fun `get context for coroutine on global scope`() {

    }

    @Test
    fun `structured concurrency with runBlocking builder`() {

    }

    @Test
    fun `effect of blocking with runBlocking`() {

    }

    @Test
    fun `use async childs on runBlocking`() {

    }

    @Test
    fun `child on global scope`() {

    }

    private suspend fun getInfo() = coroutineScope {

    }



    @Test
    fun `use couroutineScope to launch child coroutines on a suspend function`() {

    }
}