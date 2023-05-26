package isel.leic.pc.lec_05_23

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> CompletableFuture<T>.await() : T {
    return suspendCancellableCoroutine<T> { cont ->
        cont.invokeOnCancellation {
            println("cancel request occurred!")
            this.cancel(true)
        }
        this.whenComplete { v, e ->
            if (e != null) {
                cont.resumeWithException(e)
            }
            else {
                cont.resume(v)
            }

        }

    }
}