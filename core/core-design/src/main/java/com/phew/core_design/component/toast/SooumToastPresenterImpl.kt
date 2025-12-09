package com.phew.core_design.component.toast

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.phew.core_design.R

/**
 * 토스트 실제 표시/숨김 처리 기능을 제공한다.
 */
internal class SooumToastPresenterImpl(private val text: CharSequence): SooumToastPresenter {
    /**
     * View Matrix example:
     * DisplayMetrics{density=2.5, width=1080, height=2040, scaledDensity=2.5, xdpi=400.0, ydpi=400.0}
     */

    private var displayParcel: DisplayParcel? = null

    override fun getId(): Int {
        return this.hashCode()
    }

    override fun show(contextProvider: SooumToastContextProvider, gravity: Int, xOffset: Int, yOffset: Int) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "show() $text")

        displayParcel = contextProvider.getCurrentActiveContext()?.let {
            Log.v(TAG, "context: ${it.hashCode()}, $it")

            DisplayParcel(
                wm = it.getSystemService(Context.WINDOW_SERVICE) as? WindowManager,
                view = getView(it, text)
            )
        }

        // Fade In
        displayParcel?.let {
            if (it.canAddable()) {
                addViewWithAnimation(it, gravity, xOffset, yOffset)
            }
        }
    }

    override fun hide(enableAnimation: Boolean) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "hide() enableAnimation=$enableAnimation, $text")

        // Fade Out
        displayParcel?.let { dp ->
            if (dp.canRemovable()) {
                if (enableAnimation) {
                    removeViewWithAnimation(dp)
                } else {
                    removeView(dp)
                }
            } else dp.invalidate()
        }
    }

    private data class DisplayParcel(var wm: WindowManager?, var view: View?, private var isAdded: Boolean = false, private var isRemoved: Boolean = false) {
        fun canAddable() = wm != null && view != null && !isAdded
        fun canRemovable() = wm != null && view != null && isAdded && !isRemoved

        fun setAdded() { isAdded = true }
        fun setRemoved() {
            view?.visibility = View.GONE
            isRemoved = true
        }

        fun invalidate() {
            // prevent memory leak
            view?.visibility = View.GONE
            view = null
            wm = null
        }
    }

    companion object {
        private const val TAG = "SimpleToastPresenter"

        fun getInstance(text: CharSequence): SooumToastPresenter {
            return SooumToastPresenterImpl(text)
        }

        private fun getView(context: Context, text: CharSequence): View {
            val view = LayoutInflater.from(context).inflate(R.layout.view_aphrodite_toast, null)
            val textView : TextView = view.findViewById(R.id.tv_toast)
            textView.text = text
            textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START

            return view
        }

        private fun getLayoutParams(matrix: DisplayMetrics, gravity: Int, xOffset: Int, yOffset: Int) =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
            ).apply {
                // Gravity
                this.gravity = gravity
                this.x = (xOffset * matrix.density).toInt() // dp to px
                this.y = (yOffset * matrix.density).toInt()

                // Place the window within the entire screen, ignoring any constraints from the parent window.
                this.flags = this.flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            }

        private fun addViewWithAnimation(dp: DisplayParcel, gravity: Int, xOffset: Int, yOffset: Int) {

            if (dp.view != null) {
                // alpha Animation
                val alpha = ObjectAnimator.ofFloat(dp.view, "alpha", 0f, dp.view!!.alpha).apply {
                    duration = 400
                }

                // translationY Animation
                // 뷰가 나타나는 지점에서 24dp 만큼 올린다.
                val translationY = ObjectAnimator.ofFloat(dp.view, "translationY", dp.view!!.translationY, -8f).apply {
                    duration = 400
                }

                AnimatorSet().apply{
                    addListener(object : Animator.AnimatorListener{
                        override fun onAnimationStart(animation: Animator) {
                            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeIn start")

                            try {
                                dp.wm?.addView(dp.view, getLayoutParams(dp.view!!.context.resources.displayMetrics, gravity, xOffset, yOffset))
                                dp.setAdded()
                            } catch (e: Exception) {
                                if (SooumToastConstants.DEBUG_ENABLED) {
                                    e.printStackTrace()
                                    // TODO. 이미 생성된 뷰를 화면 회전 후 addView 하면 발생함.(Activity 재생성 되므로 context 가 변경됨)
                                    //android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
                                }
                            } finally {
                            }
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeIn end")
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeIn cancel")
                        }

                        override fun onAnimationRepeat(animator: Animator) {
                        }
                    })

                    playTogether(alpha, translationY)
                }.start()
            }
        }

        private fun removeViewWithAnimation(dp: DisplayParcel) {
            ObjectAnimator.ofFloat(dp.view, "alpha", dp.view!!.alpha, 0f).apply {
                duration = 400
                addListener(object : Animator.AnimatorListener{
                    override fun onAnimationStart(animation: Animator) {
                        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeOut start")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        try {
                            dp.wm?.removeView(dp.view)
                        } catch (e: Exception) {
                            if (SooumToastConstants.DEBUG_ENABLED) {
                                e.printStackTrace()
                            }
                        } finally {
                            dp.setRemoved()
                        }

                        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeOut end")
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "FadeOut cancel")
                    }

                    override fun onAnimationRepeat(animator: Animator) {
                    }
                })
            }.start()
        }

        private fun removeView(displayParcel: DisplayParcel) {
            try {
                displayParcel.wm?.removeView(displayParcel.view)
            } catch (e: Exception) {
                if (SooumToastConstants.DEBUG_ENABLED) e.printStackTrace()
            } finally {
                displayParcel.setRemoved()
            }
        }

    }

}
