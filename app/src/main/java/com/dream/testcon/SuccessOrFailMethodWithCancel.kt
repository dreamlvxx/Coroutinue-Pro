package com.dream.testcon

import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun interface Cancellable1 {
    fun onCancel()
}

interface SuccessOrFailCallback1 {
    fun onSuccess(value: String)

    fun onError(t: Throwable)
}

fun sendRequest1(callback: SuccessOrFailCallback): Cancellable1 {
    val t = thread {
        try {
            Thread.sleep(2000)
            callback.onSuccess("success")
        } catch (e: Throwable) {
            callback.onError(e)
        }
    }
    return Cancellable1 {
        t.interrupt()
    }
}

suspend fun main() {
    // 回调方式
//    sendRequest1(object : SuccessOrFailCallback {
//        override fun onSuccess(value: String) {
//            print(value)
//        }
//
//        override fun onError(t: Throwable) {
//            print(t.toString())
//        }
//    }).onCancel()

    //suspend方式

    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        try {
            print(ktRequest1())
        } catch (e: Throwable) {
            print("send $e")
        }
    }
    delay(100)
    scope.cancel()
}

suspend fun ktRequest1() = suspendCancellableCoroutine<String> { continuation ->
    val cancelable = sendRequest1(object : SuccessOrFailCallback {
        override fun onSuccess(value: String) {
            continuation.resume(value)
        }

        override fun onError(t: Throwable) {
            continuation.resumeWithException(t)
        }
    })

    continuation.invokeOnCancellation {
        cancelable.onCancel()
    }

}