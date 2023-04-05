package isel.leic.pc.lec_04_11

import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

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