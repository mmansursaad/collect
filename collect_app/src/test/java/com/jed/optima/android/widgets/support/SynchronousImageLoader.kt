package com.jed.optima.android.widgets.support

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.jed.optima.imageloader.GlideImageLoader
import com.jed.optima.imageloader.ImageLoader
import java.io.File

class SynchronousImageLoader(private val fail: Boolean = false) : ImageLoader {
    override fun loadImage(
        imageView: ImageView,
        imageFile: File?,
        scaleType: ImageView.ScaleType,
        requestListener: GlideImageLoader.ImageLoaderCallback?
    ) {
        if (fail) {
            requestListener?.onLoadFailed()
        } else {
            imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile?.absolutePath))
            requestListener?.onLoadSucceeded()
        }
    }
}
