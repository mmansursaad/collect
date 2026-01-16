package com.yedc.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yedc.android.R
import com.yedc.androidshared.ui.Animations.createAlphaAnimation

class TransparentProgressScreen(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.transparent_progress_screen, this)
    }

    override fun setVisibility(visibility: Int) {
        when (visibility) {
            GONE -> {
                super.setVisibility(visibility)
            }

            INVISIBLE -> {
                super.setVisibility(visibility)
            }

            VISIBLE -> {
                alpha = 0f
                super.setVisibility(VISIBLE)
                createAlphaAnimation(0f, 1f, 60).start()
            }
        }
    }
}
