package isel.leic.pc.lec_04_04

import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals
import kotlin.time.Duration

class SimpleFutureTests {

    @Test
    fun `callable executor with a timeout test`() {

        val fut = SimpleFuture.execute<Int> {
            Thread.sleep(1000)
            23
        }

        val num = fut.get(500, TimeUnit.MILLISECONDS)
        assertEquals(23, num)
    }
}