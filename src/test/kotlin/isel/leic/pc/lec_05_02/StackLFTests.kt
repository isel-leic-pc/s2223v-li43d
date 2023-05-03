package isel.leic.pc.lec_05_02

import isel.leic.pc.utils.launchMany
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StackLFTests {
    @Test
    fun `multiple pushers and poppers test`() {
        val stack = StackLF<Int>()
        val NTHREADS = 100
        val NTRIES = 100_000
        val result = mutableSetOf<Int>()
        val mutex = ReentrantLock()
        val errors = AtomicInteger(0)

        val threads = launchMany(NTHREADS, "stack_workers") { id ->
            try {
                var n = id
                repeat(NTRIES) {
                    stack.push(n)
                    stack.pop()
                        ?.also { value ->
                            mutex.withLock {
                                result.add(value)
                            }
                            n = value
                        }
                        ?: throw NullPointerException()
                }
            }
            catch(e: NullPointerException) {
                errors.incrementAndGet()
            }
        }

        threads.forEach { it.join(3000)}
        Assert.assertEquals("pop should not return null", 0, errors.get())
        Assert.assertEquals("all values must been popped", NTHREADS, result.size)
        Assert.assertTrue("stack should be empty", stack.isEmpty())

    }

    class IntNode(var value: Int) : INode()

    @Test
    fun `multiple pushers and poppers with intrusive list stack test`() {
        val stack = StackLFIL()
        val NTHREADS = 100
        val NTRIES = 100_000
        val result = Collections.synchronizedSet(mutableSetOf<Int>())
        val errors = AtomicInteger(0)
        val threads = launchMany(NTHREADS, "stack_workers") {
            var node = IntNode(it)
            try {
                repeat(NTRIES) {
                    stack.push(node)
                    stack.pop()?.also { n ->
                        result.add((n as IntNode).value)
                        node = n
                    } ?: throw NullPointerException()
                }
            } catch (e: NullPointerException) {
                errors.incrementAndGet()
            }
        }

        threads.forEach { it.join() }


        //Assert.assertEquals( "pop should not return null", 0, errors.get())
        //Assert.assertTrue("stack should be empty", stack.isEmpty())
        Assert.assertEquals("all values must been popped", NTHREADS, result.size)
    }
}