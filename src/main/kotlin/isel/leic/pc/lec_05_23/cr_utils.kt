package isel.leic.pc.lec_05_23

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val displayMutex = Mutex()

suspend fun CoroutineContext.show() {
    displayMutex.withLock {
        val builder = StringBuilder("start of context $this elements:\n")
        this.fold(builder) {
                builder, ctx->
            builder.append(ctx.key)
            builder.append(": ")
            builder.append(ctx.toString())
            builder.append("\n")
            builder

        }
        builder.append("end\n")
        println(builder)
    }
}

val Job.state : String
    get() {
        val builder = StringBuilder("[")
        if (isCancelled) builder.append(" Cancelled")
        if (isCompleted) builder.append(" Completed")
        if (isActive)    builder.append(" Active")
        builder.append(" ]")
        return builder.toString()
    }

suspend fun Job.show() {
    displayMutex.withLock {
        println("job $this childs:")
        for (job in children) {
            println("\t$job")
        }
        println("end")
        println("parent: ${this.parent}")
        println("state: $state")
    }

}