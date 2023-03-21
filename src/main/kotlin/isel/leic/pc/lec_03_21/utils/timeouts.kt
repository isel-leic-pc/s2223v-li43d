package isel.leic.pc.lec_03_21.utils


import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import java.lang.System.currentTimeMillis

inline val Duration.isZero : Boolean
    get() = this.inWholeMilliseconds == 0L

/**
 * utility extension functions and properties to
 * manage timeouts in absolute time
 */

// the absolute time expiration for given timeout
fun Duration.dueTime() =
    // Duration addition supports INFINITE operands without overflow
    (System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS) + this)
        .toLong(DurationUnit.MILLISECONDS)

// the remaining for timeout expiration
// (0 if already expired)
val Long.remaining : Long
    get() = if(this == Long.MAX_VALUE) Long.MAX_VALUE
    else Math.max(0L, this - currentTimeMillis())

// Condition await extension with absolute time in millis
fun Condition.await(dueTime : Long) {
    this.await(dueTime.remaining, TimeUnit.MILLISECONDS)
}

// check if timeout already occurrs
inline val Long.isPast : Boolean
    get() =  remaining == 0L


/**
 * utility extension functions and types to
 * manage timeouts in relative time
 */

class MutableTimeout(var duration: Duration) {
    val elapsed : Boolean
        get() = nanos <= 0

    var nanos : Long
        get() = duration.inWholeNanoseconds
        set(value) {
            duration = value.toDuration(DurationUnit.NANOSECONDS)
        }
}

fun Condition.await(mutableTimeout: MutableTimeout) {
    if (mutableTimeout.duration.isInfinite())
        await()
    else {
        awaitNanos(mutableTimeout.nanos).also {
            mutableTimeout.nanos = it
        }
    }
}

