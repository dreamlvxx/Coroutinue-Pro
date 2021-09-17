package com.dream.testcon
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
  单函数回调 改成 suspend方法
 */

fun interface SingleTaskCallback {
    fun onCallback(value: String)
}

fun runTask(callback: SingleTaskCallback) {
    thread {
        Thread.sleep(1000)
        callback.onCallback("result-call")
    }
}

suspend fun ktRuntask() = suspendCoroutine<String> { continutation ->
    runTask {
        continutation.resume(it)
    }
}


suspend fun main() {

    //回调使用
//    runTask(SingleTaskCallback {
//        print(it)
//    })

    //协程方式
    print(ktRuntask())
}


