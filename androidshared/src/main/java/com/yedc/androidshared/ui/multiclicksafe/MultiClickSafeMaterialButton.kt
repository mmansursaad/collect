package com.yedc.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton
import com.yedc.androidshared.R
import com.yedc.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

open class MultiClickSafeMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    private lateinit var screenName: String

    init {
        context.withStyledAttributes(attrs, R.styleable.MultiClickSafeMaterialButton) {
            screenName = getString(R.styleable.MultiClickSafeMaterialButton_screenName) ?: javaClass.name
        }
    }

    override fun performClick(): Boolean {
        return allowClick(screenName) && super.performClick()
    }
}
