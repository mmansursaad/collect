package com.yedc.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import com.yedc.analytics.Analytics
import com.yedc.android.formmanagement.FormsDataService
import com.yedc.android.injection.DaggerUtils
import com.yedc.async.TaskSpec
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
