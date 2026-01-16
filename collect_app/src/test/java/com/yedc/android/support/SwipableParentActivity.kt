package com.yedc.android.support

import androidx.fragment.app.FragmentActivity
import com.yedc.android.audio.AudioControllerView.SwipableParent

class SwipableParentActivity : FragmentActivity(), SwipableParent {
    var isSwipingAllowed = false
        private set

    override fun allowSwiping(allowSwiping: Boolean) {
        isSwipingAllowed = allowSwiping
    }
}
