package isel.leic.pc.lec_05_02.completable_futures

import mu.KotlinLogging
import java.lang.IllegalStateException
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

/*
 * Exemplo de operação assíncrona representada por CompletableFuture
 * criado e completado manualmente
 */
fun oper1Async() : CompletableFuture<Int> {

    val cf = CompletableFuture<Int>()

    thread {
        Thread.sleep(2000)
        logger.info("produce oper1Async result")
        cf.complete(2)
    }

    return cf

}

/**
 * Ilustração do lançamento de erros
 * em operações assíncronas e sua consequência em cadeias de operações
 * (ver respectivos testes unitários)
 */
fun oper2Async(id : Int) : CompletableFuture<String> {
    val cf = CompletableFuture<String>()

    thread {
        Thread.sleep(2000)
        logger.info("produce oper2Async result")
        if (id < 0)
            cf.completeExceptionally(IllegalStateException("bad string production"))
        cf.complete("hello")
    }

    return cf

}

/**
 * Exemplo de operação assíncrona representado por CompletableFuture
 * criado a partir de método factory da classe CompletableFuture
 */
fun oper3Async(name : String) : CompletableFuture<Int> {
    return CompletableFuture.supplyAsync {
        Thread.sleep(2000)
        logger.info("produce oper3Async result")
        3
    }
}

