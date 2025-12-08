package com.phew.core_design.component.toast

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 토스트 표시/숨김 처리 작업규 관리 쓰레드 (Singleton)
 */
internal class SooumToastThreadImpl(
    private val contextProvider: SooumToastContextProvider
): SooumToastThread {

    private val toasts = ConcurrentLinkedQueue<ST>()

    private var thread: ToastLooperImpl? = null
    private val handler = Handler(Looper.getMainLooper())


    override fun enqueue(
        presenter: SooumToastPresenter,
        duration: Int,
        gravity: Int,
        xOffset: Int,
        yOffset: Int,
        delay: Long
    ) {

        if (SooumToastConstants.DEBUG_ENABLED) {
            Log.v(TAG, "enqueue() ${presenter.getId()} toast size : ${toasts.size}")
        }

        if (toasts.size > MAX_LIMIT) {
            toasts.poll()
            if (SooumToastConstants.DEBUG_ENABLED) {
                Log.w(TAG, "too many request. head was removed.")
            }
        }


        // 토스트 요청 추가
        toasts.add(ST(presenter, duration, gravity, xOffset, yOffset, delay))

        // 토스트 쓰래드 준비
        prepareLooper()

        // 즉시 표시 요청
        if (SooumToastConstants.INSTANT_MODE_ENABLED) {
            thread?.showImmediately()
        }
    }

    override fun cancel(presenter: SooumToastPresenter) {
        if (SooumToastConstants.DEBUG_ENABLED) {
            Log.v(TAG, "cancel() ${presenter.getId()}")
        }

        toasts.firstOrNull { it.presenter.getId() == presenter.getId() }?.let {
            toasts.remove(it)
        }
    }

    override fun cancelAll() {
        if (SooumToastConstants.DEBUG_ENABLED) {
            Log.v(TAG, "cancelAll()")
        }
        toasts.clear()
    }

    private fun prepareLooper() {
        if (thread == null || thread?.state == Thread.State.TERMINATED) {
            thread = ToastLooperImpl().apply {
                start()
            }
        }
    }


    /**
     * duration 단위로 작업 큐의 토스트를 꺼내서 toast presenter 로 전달한다.
     */
    private inner class ToastLooperImpl: Thread() {
        private var lock = Object()
        // 현재 화면에 표시중 토스트
        private var inDisplaying: ST? = null
        private var isHideImmediately = AtomicBoolean(false)

        override fun run() {
            if (SooumToastConstants.DEBUG_ENABLED) {
                Log.v(TAG, "toast looper started. q.size=${toasts.size}")
            }

            try {
                while (true) {
                    inDisplaying?.let {
                        // 표시 시간 만큼 대기 - kick() 에 의해 깨어남
                        waitIfNeed(it.getTTL())

                        //
                        if (isHideImmediately.getAndSet(false)) {
                            if (SooumToastConstants.DEBUG_ENABLED) {
                                Log.v(TAG, "cancel previous toast")
                            }
                            // 에니메이션 없이 즉시 사라지게 한다.
                            handler.post(Runnable {
                                it.presenter.hide(enableAnimation = false)
                            })
                        }
                    }

                    val st = toasts.poll().also {
                        inDisplaying = it
                    }

                    if (st == null) {
                        if (SooumToastConstants.DEBUG_ENABLED) {
                            Log.v(TAG, "empty toast queue")
                        }
                        break

                    } else {
                        if (SooumToastConstants.DEBUG_ENABLED) {
                            Log.v(TAG, "toast poll. id=${st.presenter.getId()}, delay=${st.delayMillis}")
                        }

                        // 토스트 표시 요청
                        handler.postDelayed(Runnable {
                            st.initExpireAt()
                            st.presenter.show(contextProvider, st.gravity, st.xOffset, st.yOffset)

                            // destroy 콜백 등록
                            contextProvider.setDestroyCallback {
                                handler.post(Runnable {
                                    st.presenter.hide(enableAnimation = false)
                                    kick()
                                })
                            }

                        }, st.delayMillis)

                        // 토스트 표시 취소 요청
                        handler.postDelayed(Runnable {
                            st.presenter.hide()
                        }, toMillis(st.duration) + st.delayMillis)

                    }
                } // loop

            } catch (e: Exception) {
                if (SooumToastConstants.DEBUG_ENABLED) {
                    e.printStackTrace()
                }
            } finally {
                inDisplaying = null
            }


            if (SooumToastConstants.DEBUG_ENABLED) {
                Log.v(TAG, "toast looper finished")
            }
        }

        private fun waitIfNeed(timeoutMillis: Long = 5000L) {
            if (timeoutMillis <= 0L) return

            try {
                if (SooumToastConstants.DEBUG_ENABLED) {
                    Log.v(TAG, "waiting: $timeoutMillis ms")
                }

                synchronized(lock) {
                    lock.wait(timeoutMillis)
                }
            } catch (_: Exception) {
            }
        }

        private fun kick() {
            try {
                if (SooumToastConstants.DEBUG_ENABLED) {
                    Log.v(TAG, "kick()")
                }

                synchronized(lock) {
                    lock.notifyAll()
                }
            } catch (_: Exception) {
            }
        }

        fun showImmediately() {
            if (inDisplaying != null) {
                isHideImmediately.set(true)
                kick()
            }
        }
    }


    private data class ST(
        val presenter: SooumToastPresenter,
        val duration: Int,
        val gravity: Int,
        val xOffset: Int,
        val yOffset: Int,
        private val delay: Long
    ) {
        private var expiredAt = 0L
        val delayMillis: Long
            get() = if (delay > 0L) delay else 0L

        fun initExpireAt() {
            expiredAt = System.currentTimeMillis() + toMillis(duration) + delayMillis
        }

        fun getTTL(): Long {
            val currentMillis = System.currentTimeMillis()
            return when {
                expiredAt == 0L -> toMillis(duration) + delayMillis
                currentMillis >= expiredAt -> 0L
                else -> expiredAt - currentMillis
            }
        }
    }
    companion object {
        private const val TAG = "AphroditeToastThread"

        private const val MAX_LIMIT = 5

        private fun toMillis(duration: Int): Long {
            return when (duration) {
                Toast.LENGTH_SHORT -> 3000L
                Toast.LENGTH_LONG -> 5000L
                else -> 3000L
            }
        }

        @Volatile
        private lateinit var INSTANCE: SooumToastThreadImpl

        fun initToastThread(contextProvider: SooumToastContextProvider): SooumToastThread {
            synchronized(this) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = SooumToastThreadImpl(contextProvider)
                }
                return INSTANCE
            }
        }

        fun getThread(): SooumToastThread? {
            synchronized(this) {
                return if (Companion::INSTANCE.isInitialized) {
                    INSTANCE
                } else {
                    null
                }
            }
        }
    }
}