package isel.leic.pc.lec_05_04

import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis



/**
 * tests for checking the thread safety and efficiency of different types of
 * map implementations
 */
class MapTests {
    /**
     * the scenario simulates the generation of a word counting
     * in NLINES of text (for simplification we simulate the existence of
     * a single word in each line)
     */
    private val random = Random()

    private val NLINES = 4_000_000  // number of lines

    // for define the number of partitions (threads)
    // and the size of each slot
    private val SLOTS = 4
    private val SLOT_SIZE = NLINES / SLOTS

    // the (simulated) existent words
    private val words = IntRange(0,1000).map { it.toString() }


    private fun randomInRange(min: Int, max : Int) : Int =
        Math.abs(random.nextInt()) % (max - min + 1) + min

    /**
     * this is a specialized map that have a (thread safe) operation to
     * increment the count of a given word
     */
    private class SpecializedWordCounterMap {
        val map = HashMap<String, AtomicInteger>()
        val mutex = ReentrantLock()

        fun increment(key: String) {
            mutex.withLock {
                map[key] = map[key]?.also { it.incrementAndGet() } ?: AtomicInteger(1)
            }
        }
    }

    /**
     * an auxiliary function used to fill the different maps
     * given the update function received
     */
    private fun buildMap(update : (String) -> Unit) {
        val threads =   (0 until SLOTS)
            .map {
                thread {
                    for (line in it until it + SLOT_SIZE) {
                        update(words.get(randomInRange(0, words.size - 1)))
                    }
                }
            }
        threads.forEach { it.join()}
    }

    private fun buildSimpleMap() : Map<String, AtomicInteger> {
        val map = HashMap<String, AtomicInteger>()

        buildMap {key->
            //map[key] = map[key]?.also { it.incrementAndGet() } ?: AtomicInteger(1)
            map.computeIfAbsent(key) { AtomicInteger() }.incrementAndGet()
        }
        return map
    }

    /**
     * builds a map using a JVM synchronized map
     * A synchronized is just a wrapper for a non thread safe map
     * that execute each of the map operations in the possession
     * of an internal lock
     */
    private fun buildSynchronizedMap() : Map<String, AtomicInteger> {
        val map = Collections.synchronizedMap(HashMap<String, AtomicInteger>())

        buildMap {key->
            map[key] = map[key]?.also { it.incrementAndGet() } ?: AtomicInteger(1)
        }
        return map
    }

    /**
     * A ConcurrentHashMap is a thread safe map
     * that uses fine-grained lock (one per slot) and CAS
     * to increase the scalability with the increase of the number
     * of client threads
     * map.computeIfAbsent(key) { MutableInt() }.increment()
     */
    private fun buildConcurrentMap() : Map<String, AtomicInteger> {
        val map = ConcurrentHashMap<String, AtomicInteger>()
        buildMap {key ->
            //map[key] = map[key]?.also { it.incrementAndGet() } ?: AtomicInteger(1)
            map.computeIfAbsent(key) { AtomicInteger(0) }.incrementAndGet()
        }
        return map
    }

    /**
     * this creates a word counter map using the SpecializedWordCounterMap
     * defined above
     */
    private fun buildSpecializedWordCounterMap() : Map<String, AtomicInteger> {
        val map = SpecializedWordCounterMap()
        buildMap {
                key-> map.increment(key)
        }
        return map.map
    }

    /**
     * A generic test function that evaluates and shows the execution time
     */
    private fun doTest(builder : () -> Map<String, AtomicInteger>, name : String) : Int{

        var count = 0
        val duration = measureTimeMillis {
            val map = builder()

            count =
                map.values.reduce { m1, m2 ->
                    AtomicInteger(m1.get() + m2.get())
                }.get()

        }
        println("$name in $duration ms")
        return count
    }

    /**
     * first, testing with a non thread safe map
     */
    @Test
    fun `simple map test`() {
        val expectedCount = NLINES
        val count = doTest(::buildSimpleMap, "simple map test")

        Assert.assertEquals(expectedCount, count)
    }

    @Test
    fun `synchronized map test`() {

        val expectedCount = NLINES
        val count = doTest(::buildSynchronizedMap, "synchronized map test")

        Assert.assertEquals(expectedCount, count)

    }


    @Test
    fun `concurrent map test`() {
        val expectedCount = NLINES
        val count = doTest(::buildConcurrentMap, "concurrent map test")

        Assert.assertEquals(expectedCount, count)

    }

    @Test
    fun `specialized word counter map test`() {
        val expectedCount = NLINES
        val count = doTest(::buildSpecializedWordCounterMap, "specialized word counter map test")

        Assert.assertEquals(expectedCount, count)
    }

}