package com.yedc.android.application.initialization

import com.yedc.async.Scheduler
import com.yedc.projects.ProjectsRepository
import com.yedc.upgrade.Upgrade

/**
 * Reschedule all background work to prevent problems with tag or class name changes etc
 */
class ScheduledWorkUpgrade(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val formUpdateScheduler: _root_ide_package_.com.yedc.android.backgroundwork.FormUpdateScheduler,
    private val instanceSubmitScheduler: _root_ide_package_.com.yedc.android.backgroundwork.InstanceSubmitScheduler
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
