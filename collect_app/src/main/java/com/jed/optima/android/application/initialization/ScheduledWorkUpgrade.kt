package com.jed.optima.android.application.initialization

import com.jed.optima.async.Scheduler
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.upgrade.Upgrade

/**
 * Reschedule all background work to prevent problems with tag or class name changes etc
 */
class ScheduledWorkUpgrade(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val formUpdateScheduler: com.jed.optima.android.backgroundwork.FormUpdateScheduler,
    private val instanceSubmitScheduler: com.jed.optima.android.backgroundwork.InstanceSubmitScheduler
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        scheduler.cancelAllDeferred()

        projectsRepository.getAll().forEach {
            formUpdateScheduler.scheduleUpdates(it.uuid)
            instanceSubmitScheduler.scheduleAutoSend(it.uuid)
            instanceSubmitScheduler.scheduleFormAutoSend(it.uuid)
        }
    }
}
