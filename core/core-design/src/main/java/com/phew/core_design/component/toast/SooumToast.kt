package com.phew.core_design.component.toast

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import com.phew.core_design.component.toast.SooumToast.Companion.init


/**
 * 토스트 기능을 제공한다.
 * + 토스트는 요청된 순서대로 작업 큐에 적재되며 순차적으로 표시된다.
 * + 토스트는 표시 시점의 활성화된 액티비티의 context 에 의존한다.
 * + 토스트 기능을 사용하기 위해서는 Application 클래스 내에서 [init] 호출하여야 한다.
 * + 지연된 토스트 표시가 필요한 경우 [showDelayed] 를 호출할 것.(지정된 지연 시간 후 토스트가 표시된다)
 */
class SooumToast(private val presenter: SooumToastPresenter) {
    /**
     * 토스트 표시 유지 시간
     * + [SooumToast.LENGTH_LONG], [SooumToast.LENGTH_SHORT] 값을 참고할 것
     * + 기본값: [LENGTH_SHORT]
     */
    var duration: Int = LENGTH_SHORT

    private var gravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    private var xOffset = 0
    private var yOffset = 66 // 기본값

    /**
     * Gravity 값을 설정한다.
     * + [gravity] 는 [Gravity.TOP] ~ [Gravity.FILL] 의 값을 사용할 수 있다.
     * + [xOffset], [yOffset] 의 단위는 dp 이다.
     * + 기본값: [gravity]=[Gravity.BOTTOM], [xOffset]=0, [yOffset]=48
     */
    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "setGravity() $gravity, $xOffset, $yOffset")
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
    }

    /**
     * 토스트 표시하도록 요청한다.
     */
    fun show() {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "show() ${presenter.getId()}")

        SooumToastThreadImpl.Companion.getThread()
            ?.enqueue(presenter, duration, gravity, xOffset, yOffset, 0L)
    }

    /**
     * 지연 시간 [delay] (단위: 밀리초) 후 토스트를 화면에 표시한다.
     * + 토스트 후 즉시 다른 액티비티로 이동하는 경우에 사용한다.
     */
    fun showDelayed(delay: Long = 850L) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "showDelayed() ${presenter.getId()}")

        SooumToastThreadImpl.Companion.getThread()
            ?.enqueue(presenter, duration, gravity, xOffset, yOffset, delay)
    }

    /**
     * 토스트 표시 취소를 요청한다.
     */
    fun cancel() {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "cancel() ${presenter.getId()}")

        SooumToastThreadImpl.Companion.getThread()?.cancel(presenter)
    }


    companion object {
        private const val TAG = "SooumToast"

        /**
         * 토스트 표시 시간: LONG (5초)
         */
        const val LENGTH_LONG = Toast.LENGTH_LONG

        /**
         * 토스트 표시 시간: SHORT (3초)
         */
        const val LENGTH_SHORT = Toast.LENGTH_SHORT

        /**
         * 토스트 기능 사용을 위한 초기화 작업을 수행한다.
         * + Application 클래스 내에서 호출해야 한다.
         */
        fun init(app: Application) {
            val contextProvider = SooumToastLifecycleCallbacks()
            app.registerActivityLifecycleCallbacks(contextProvider)
            SooumToastThreadImpl.Companion.initToastThread(contextProvider)
        }

        /**
         * 리소스 ID [resId] 에 해당하는 텍스트를 가져와 토스트를 생성한다.
         */
        fun makeToast(context: Context, @StringRes resId: Int, duration: Int, yOffset: Int? = null): SooumToast {
            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "makeToast() $resId, $duration, yOffset=$yOffset")

            val text = context.getString(resId)
            return makeSooumToast(text, duration, yOffset)
        }

        fun makeToast(context: Context, message: String, duration: Int, yOffset: Int? = null): SooumToast {
            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "makeToast() $message, $duration, yOffset=$yOffset")
            return makeSooumToast(message, duration, yOffset)
        }

        /**
         * 특정 텍스트 [text] 에 해당하는 토스트를 생성한다.
         * + [duration] 은 [LENGTH_LONG],[LENGTH_SHORT] 값을 지정할 수 있다.
         */
        fun makeToast(text: CharSequence, duration: Int, yOffset: Int? = null): SooumToast {
            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "makeToast() $text, $duration, yOffset=$yOffset")

            return makeSooumToast(text, duration, yOffset)
        }

        /**
         * 아직 표시되지 않은 토스트들의 표시를 취소한다.
         */
        fun cancel() {
            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "cancel()")

            SooumToastThreadImpl.Companion.getThread()?.cancelAll()
        }

        private fun makeSooumToast(text: CharSequence, duration: Int, yOffset: Int? = null): SooumToast {
            val toast = SooumToast(SooumToastPresenterImpl.getInstance(text)).apply {
                this.duration = duration
            }
            yOffset?.let {
                toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, it)
            }
            return toast
        }
    }
}
