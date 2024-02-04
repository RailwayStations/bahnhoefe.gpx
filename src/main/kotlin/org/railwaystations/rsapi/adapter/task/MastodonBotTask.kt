package org.railwaystations.rsapi.adapter.task

import org.railwaystations.rsapi.core.services.InboxService
import org.railwaystations.rsapi.utils.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MastodonBotTask(private val inboxService: InboxService) {

    private val log by Logger()

    @Scheduled(fixedRate = 60000 * 60) // every hour
    fun postNewPhoto() {
        log.info("Starting MastodonBotTask")
        inboxService.postRecentlyImportedPhotoNotYetPosted()
    }
}
