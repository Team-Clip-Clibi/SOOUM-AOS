package com.phew.core_common.log

import android.util.Log
import com.phew.core_common.BuildConfig

object SooumLog {
    private const val TAG = "SooumLog"
    private const val EMPTY = "*"
    private const val CHUNK_SIZE = 1024

    fun v(tag: String = TAG, msg: String = EMPTY) {
        if (BuildConfig.DEBUG) {
            msg.chunked(CHUNK_SIZE).forEach {
                Log.v(tag, it)
            }
        }
    }

    fun d(tag: String = TAG, msg: String = EMPTY) {
        if (BuildConfig.DEBUG) {
            msg.chunked(CHUNK_SIZE).forEach {
                Log.d(tag, it)
            }
        }
    }

    fun i(tag: String = TAG, msg: String = EMPTY) {
        if (BuildConfig.DEBUG) {
            msg.chunked(CHUNK_SIZE).forEach {
                Log.i(tag, it)
            }
        }
    }

    fun w(tag: String = TAG, msg: String = EMPTY) {
        if (BuildConfig.DEBUG) {
            msg.chunked(CHUNK_SIZE).forEach {
                Log.w(tag, it)
            }
        }
    }

    fun e(tag: String = TAG, msg: String = EMPTY) {
        if (BuildConfig.DEBUG) {
            msg.chunked(CHUNK_SIZE).forEach {
                Log.e(tag, it)
            }
        }
    }
}