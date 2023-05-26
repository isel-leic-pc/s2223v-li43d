package isel.leic.pc.lec_05_23


import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Test
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

class ScopeTests {


    @Test
    fun `manual scope creation`() {
        runBlocking {
            // manual scope creation
            val scope = CoroutineScope(Job() + Dispatchers.IO)

            val child1 = scope.launch(CoroutineName("child 1")) {
                coroutineContext.show()
                delay(1000)
                // note the thread running this coroutine
                logger.info("${coroutineContext[CoroutineName]?.name} done!")
            }

            val child2 = scope.launch(CoroutineName("child 2")) {
                    coroutineContext.show()

                    delay(2000)
                    // note the thread running this coroutine
                    logger.info("${coroutineContext[CoroutineName]?.name} done!")

            }

            scope.coroutineContext.job.show()

            val parentJob = scope.coroutineContext.job as CompletableJob

            parentJob.complete()

            // comment this line and check the result
            //parentJob.join()

            logger.info("scope jobs finished!")
        }

        println("runBlocking terminate")
    }

}