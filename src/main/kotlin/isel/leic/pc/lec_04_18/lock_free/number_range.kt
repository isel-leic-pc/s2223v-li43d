package isel.leic.pc.lec_04_18.lock_free

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

// Must keep the invariant lower <= upper!
class UnsafeNumberRange(private var _lower: Int, private var _upper:Int) {
    init {
        if (_lower > _upper)
            throw  IllegalArgumentException();
    }

    private val lower = AtomicInteger(_lower)
    private val upper = AtomicInteger(_upper)


    fun setLower(l : Int) {
        if (l > upper.get() )
            throw IllegalArgumentException()
        lower.set(l)
    }

    fun setUpper(u : Int) {
        if (u < lower.get())
            throw  IllegalArgumentException();
        upper.set(u)
    }
}

class SafeNumberRangeBad(private var _lower: Int, private var _upper:Int) {
    init {
        if (_lower > _upper)
            throw  IllegalArgumentException();
    }

    private val lower = AtomicInteger(_lower)
    private val upper = AtomicInteger(_upper)


    fun setLower(l : Int) {
        do {
            if (l > upper.get())
                throw IllegalArgumentException()
            val obsLower = lower.get()
            if (lower.compareAndSet(obsLower, l))
                return
        }
        while(true)
    }

    fun setUpper(u : Int) {
        if (u < lower.get())
            throw  IllegalArgumentException();
        upper.set(u)
    }
}

class SafeNumberRange(private var _lower: Int, private var _upper:Int) {
    init {
        if (_lower > _upper)
            throw  IllegalArgumentException();
    }

    private class Range(_lower: Int, _upper : Int) {

        val lower = _lower
        val upper = _upper
    }

    private val range = AtomicReference<Range>(Range(_lower, _upper))

    fun setLower(l : Int) {
        do {
            val obsRange = range.get()
            if (l > obsRange.upper)
                throw IllegalArgumentException()
            if (range.compareAndSet(obsRange, Range(l, obsRange.upper)))
                return
        }
        while(true)
    }

    fun setUpper(u : Int) {
        do {
            val obsRange = range.get()
            if (u < obsRange.lower)
                throw IllegalArgumentException()
            if (range.compareAndSet(obsRange, Range(obsRange.lower, u)))
                return
        }
        while(true)
    }
}