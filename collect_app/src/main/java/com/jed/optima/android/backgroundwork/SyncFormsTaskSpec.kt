package com.jed.optima.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.formmanagement.FormsDataService
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.async.TaskSpec
import java.util.function.Supplier
import javax.inject.Inject

class SyncFormsTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    override val maxRetries = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(context: Context, inputData: Map<String, String>, isLastUniqueExecution: Boolean): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                formsDataService.matchFormsWithServer(projectId, isLastUniqueExecution)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }
}
