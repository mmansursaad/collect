package com.jed.optima.androidshared.async

import com.jed.optima.androidshared.livedata.MutableNonNullLiveData
import com.jed.optima.androidshared.livedata.NonNullLiveData
import com.jed.optima.async.Scheduler
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Supplier

class TrackableWorker(private val scheduler: Scheduler) {

    private val _isWorking = MutableNonNullLiveData(false)
    val isWorking: NonNullLiveData<Boolean> = _isWorking

    private var activeBackgroundJobsCounter = AtomicInteger(0)

    fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        activeBackgroundJobsCounter.incrementAndGet()
        _isWorking.value = true
        scheduler.immediate(background) { result ->
            if (activeBackgroundJobsCounter.decrementAndGet() == 0) {
                _isWorking.value = false
            }
            foreground.accept(result)
        }
    }

    fun immediate(background: Runnable) {
        immediate(
            background = {
                background.run()
            },
            foreground = {}
        )
    }
}
