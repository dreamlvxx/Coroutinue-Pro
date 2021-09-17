package com.dream.testcon

import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


interface SuccessOrFailCallback {
    fun onSuccess(value: String)

    fun onError(t: Throwable)
}

fun sendRequest(callback: SuccessOrFailCallback) {
    val t = thread {
        try {
            Thread.sleep(2000)
            callback.onSuccess("success")
        } catch (e: Throwable) {
            callback.onError(e)
        }
    }
}

suspend fun main() {
    // 回调方式
//    sendRequest(object : SuccessOrFailCallback {
//        override fun onSuccess(value: String) {
//            print(value)
//        }
//
//        override fun onError(t: Throwable) {
//            print(t.toString())
//        }
//    })

    //suspend方式
    try {
        print(ktRequest())
    } catch (e: Throwable) {
        print(e.toString())
    }
}

suspend fun ktRequest() = suspendCoroutine<String> { continuation ->
    sendRequest(object : SuccessOrFailCallback {
        override fun onSuccess(value: String) {
            continuation.resume(value)
        }

        override fun onError(t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
}