package com.jed.optima.android.support.pages

import com.jed.optima.testshared.WaitFor

class AsyncPage<T : Page<T>>(private val destination: T) : Page<T>() {
    override fun assertOnPage(): T {
        return WaitFor.waitFor {
            destination.assertOnPage()
        }
    }
}
