package com.dream.testcon

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlin.concurrent.thread

fun interface Cancelable2 {
    fun cancel()
}

interface MultiPathCallback<T> {
    fun onProgress(value: Int)

    fun onResult(value: T)

    fun onError(t: Throwable)

    fun onComplete()

}

fun startTask(callback: MultiPathCallback<String>): Cancelable2 {
    val t = thread {
        try {
            (0..100).forEach {
                Thread.sleep(10)
                callback.onProgress(it)
            }
            callback.onResult("Done")
            callback.onComplete()
        } catch (e: Throwable) {
            callback.onError(e)
        }

    }
    return Cancelable2 {
        println("cancel exe")
        t.interrupt()
    }
}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
suspend fun main() {
    //回调方式
//    startTask(object : MultiPathCallback<String> {
//        override fun onProgress(value: Int) {
//            print(value)
//        }
//
//        override fun onResult(value: String) {
//            print("   result = $value")
//        }
//
//        override fun onError(t: Throwable) {
//            print(t.toString())
//        }
//
//        override fun onComplete() {
//            print("   done is execute")
//        }
//    })

    //suspend方式
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        startTaskAsFlow().collect {
            when (it) {
                OnComplete -> print("when -- done")
                is OnError -> print("Error -- ${it.t}")
                is OnProgress -> print("\nProgress -- ${it.value}")
                is OnResult<*> -> print("Result -- ${it.value}")
            }
        }
    }.join()

//    delay(100)
//    scope.cancel()
}

sealed interface Event
class OnProgress(val value: Int) : Event
class OnError(val t: Throwable) : Event
class OnResult<T>(val value: T) : Event
object OnComplete : Event


@ExperimentalCoroutinesApi
fun startTaskAsFlow() = callbackFlow {
    val cancelable = startTask(object : MultiPathCallback<String> {
        override fun onProgress(value: Int) {
            sendBlocking(OnProgress(value))
        }

        override fun onResult(value: String) {
            sendBlocking(OnResult(value))
        }

        override fun onError(t: Throwable) {
            sendBlocking(OnError(t))
        }

        override fun onComplete() {
            sendBlocking(OnComplete)
            //完成之后要主动关闭,否则awaitClose无法关闭，一直挂起，程序无法执行完
            close()
        }
    })

    awaitClose {
        cancelable.cancel()
    }
}.conflate()













