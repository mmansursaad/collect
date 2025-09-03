package com.jed.optima.androidshared.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

object AppBarUtils {

    @JvmStatic
    fun setupAppBarLayout(activity: Activity, title: CharSequence) {
        val toolbar = activity.findViewById<Toolbar>(com.jed.optima.androidshared.R.id.toolbar)
        if (toolbar != null && activity is AppCompatActivity) {
            toolbar.title = title
            activity.setSupportActionBar(toolbar)
        }
    }
}
