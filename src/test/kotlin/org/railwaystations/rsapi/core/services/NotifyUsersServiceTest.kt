package org.railwaystations.rsapi.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.railwaystations.rsapi.adapter.db.InboxDao
import org.railwaystations.rsapi.adapter.db.UserDao
import org.railwaystations.rsapi.app.config.MessageSourceConfig
import org.railwaystations.rsapi.core.model.InboxEntry
import org.railwaystations.rsapi.core.model.License
import org.railwaystations.rsapi.core.model.User
import org.railwaystations.rsapi.core.ports.Mailer
import java.time.Instant
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NotifyUsersServiceTest {
    private val userDao: UserDao = mockk<UserDao>()
    private val inboxDao: InboxDao = mockk<InboxDao>(relaxed = true)
    private val mailer: Mailer = mockk<Mailer>(relaxed = true)
    private val service: NotifyUsersService =
        NotifyUsersService(userDao, inboxDao, mailer, MessageSourceConfig().messageSource())

    @ParameterizedTest
    @MethodSource("provideUsersToNotNotify")
    fun doNotSendEmail(user: User?) {
        every { inboxDao.findInboxEntriesToNotify() } returns createInboxEntriesToNotify()
        every { userDao.findById(1) } returns user
        service.notifyUsers()
        verify(exactly = 0) { mailer.send(any(), any(), any()) }
    }

    @Test
    fun sendEmail() {
        every { inboxDao.findInboxEntriesToNotify() } returns createInboxEntriesToNotify()
        every { userDao.findById(1) } returns createUser("nickname@example.com", User.EMAIL_VERIFIED, true)
        service.notifyUsers()
        verify {
            mailer.send(
                "nickname@example.com", "Railway-Stations.org review result", """
                Hallo nickname,

                vielen Dank für Deine Beiträge.

                Viele Grüße
                Dein Bahnhofsfoto-Team

                ---------------------------------

                1. Title 1: accepted
                2. Title 2: rejected - rejectedReason
                
                """.trimIndent()
            )
        }
    }

    private fun createInboxEntriesToNotify(): List<InboxEntry> {
        return listOf(
            createInboxEntry(1, "1", "Title 1", null),
            createInboxEntry(2, "2", "Title 2", "rejectedReason")
        )
    }

    private fun createInboxEntry(id: Int, stationId: String, title: String, rejectReason: String?): InboxEntry {
        return InboxEntry(
            id = id.toLong(),
            countryCode = "de",
            stationId = stationId,
            title = title,
            photographerId = 1,
            photographerNickname = "nickname",
            photographerEmail = "nickname@example.com",
            rejectReason = rejectReason,
            createdAt = Instant.now(),
        )
    }

    companion object {

        @JvmStatic
        private fun provideUsersToNotNotify(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(createUser(null, null, true)),
                Arguments.of(createUser("nickname@example.com", null, true)),
                Arguments.of(createUser("nickname@example.com", User.EMAIL_VERIFIED, false))
            )
        }

        private fun createUser(email: String?, emailVerification: String?, sendNotifications: Boolean): User {
            return User(
                id = 1,
                name = "nickname",
                license = License.CC0_10,
                email = email,
                ownPhotos = true,
                emailVerification = emailVerification,
                sendNotifications = sendNotifications,
                locale = Locale.GERMAN,
            )
        }
    }
}