package isel.leic.pc.lec_05_25.asynchronizers

import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

// to simplify time scheduled work
object scheduler  {
    private val pool = Executors.newSingleThreadScheduledExecutor()

    fun schedule(millis : Long, cmd : () -> Unit) : ScheduledFuture<*> =
        pool.schedule(cmd, millis, TimeUnit.MILLISECONDS)


    fun schedule(timeout : Duration, cmd : () -> Unit) : ScheduledFuture<*> =
        schedule(timeout.inWholeMilliseconds, cmd)

    fun close() {
        pool.shutdown()
    }
}