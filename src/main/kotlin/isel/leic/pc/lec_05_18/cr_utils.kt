package isel.leic.pc.lec_05_18

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.show() {
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

fun Job.showChildren() {
    println("start of job $this childs:")
    for (job in children) {
        println("\t$job")
    }
    println("end")
}