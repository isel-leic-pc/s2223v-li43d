package isel.leic.pc.lec_03_14

import mu.KotlinLogging
import org.junit.Test
import kotlin.concurrent.thread
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

class QueueTests {

    @Test
    fun `one reader and one writer queue test`() {
        val QUEUE_CAPACITY = 5
        val NOPERS = 30
        val TIMEOUT = 10000L

        val queue = Queue<Int>(QUEUE_CAPACITY)
        val result = IntArray(NOPERS)

        val writer = thread {
            repeat(NOPERS) {
                queue.put(it + 1)
                logger.info("put ${it+1}")
            }
        }

        val reader = thread {
            repeat(NOPERS) {
                Thread.sleep(100)
                val e = queue.get()
                result[it] = e
                logger.info("get $e")
            }
        }

        reader.join(TIMEOUT)
        writer.join(TIMEOUT)

        // check result
        for (i in 0 until NOPERS)
            assertEquals(i+1, result[i])
    }
}