package isel.leic.pc.lec_03_14

import mu.KotlinLogging
import org.junit.Test
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class QueueTests {

    @Test
    fun `one reader and one writer queue test`() {
        val QUEUE_CAPACITY = 5
        val NOPERS = 30
        val TIMEOUT = 10000L

        val queue = Queue<Int>()

        val writer = thread {
            repeat(NOPERS) {
                queue.put(it + 1)
                logger.info("put ${it+1}")
            }
        }

        val reader = thread {
            repeat(NOPERS) {
                val e = queue.get()
                logger.info("get $e")
            }
        }

        reader.join(TIMEOUT)
        writer.join(TIMEOUT)
    }
}