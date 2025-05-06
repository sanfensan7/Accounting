package com.example.accounting.utils

import android.util.Log

/**
 * 日志工具类
 */
object LogUtils {
    private const val APP_TAG = "AccountingApp"
    private var isDebug = true

    fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d("$APP_TAG:$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (isDebug) {
            Log.i("$APP_TAG:$tag", message)
        }
    }

    fun w(tag: String, message: String) {
        if (isDebug) {
            Log.w("$APP_TAG:$tag", message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.e("$APP_TAG:$tag", message, throwable)
            } else {
                Log.e("$APP_TAG:$tag", message)
            }
        }
    }
} 