package org.railwaystations.rsapi.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.railwaystations.rsapi.adapter.db.OAuth2AuthorizationDao
import org.railwaystations.rsapi.adapter.db.UserDao
import org.railwaystations.rsapi.adapter.monitoring.FakeMonitor
import org.railwaystations.rsapi.adapter.web.controller.ProfileControllerTest
import org.railwaystations.rsapi.app.auth.LazySodiumPasswordEncoder
import org.railwaystations.rsapi.app.config.MessageSourceConfig
import org.railwaystations.rsapi.core.model.License
import org.railwaystations.rsapi.core.model.User
import org.railwaystations.rsapi.core.ports.Mailer
import org.railwaystations.rsapi.core.ports.ManageProfileUseCase.ProfileConflictException

internal class ProfileServiceTest {
    private val userDao: UserDao = mockk<UserDao>()

    private val monitor = FakeMonitor()

    private val mailer: Mailer = mockk<Mailer>(relaxed = true)

    private val authorizationDao: OAuth2AuthorizationDao = mockk<OAuth2AuthorizationDao>(relaxed = true)

    private val sut = ProfileService(
        monitor = monitor,
        mailer = mailer,
        userDao = userDao,
        authorizationDao = authorizationDao,
        eMailVerificationUrl = "EMAIL_VERIFICATION_URL",
        passwordEncoder = LazySodiumPasswordEncoder(),
        messageSource = MessageSourceConfig().messageSource()
    )

    @BeforeEach
    fun setup() {
        every { userDao.addUsernameToBlocklist(any()) } returns Unit
        every { userDao.anonymizeUser(any()) } returns Unit
        every { userDao.countBlockedUsername(any()) } returns 0
        every { userDao.findByEmail(any()) } returns null
        every { userDao.findByNormalizedName(any()) } returns null
        every { userDao.findByEmailVerification(any()) } returns null
        every { userDao.insert(any(), any(), any()) } returns 0
        every { userDao.update(any(), any()) } returns Unit
        every { userDao.updateCredentials(any(), any()) } returns Unit
        every { userDao.updateEmailVerification(any(), any()) } returns Unit
    }

    @Test
    fun testRegisterInvalidData() {
        val newUser = User(
            name = "nickname",
            license = License.CC0_10,
            ownPhotos = true,
        )
        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun registerNewUser() {
        val newUser = createNewUser()

        sut.register(newUser, USER_AGENT)

        verify { userDao.findByNormalizedName(ProfileControllerTest.USER_NAME) }
        verify { userDao.countBlockedUsername(ProfileControllerTest.USER_NAME) }
        verify { userDao.findByEmail(ProfileControllerTest.USER_EMAIL) }
        verify { userDao.insert(any(), any(), any()) }
        verify(exactly = 0) { userDao.updateCredentials(any(), any()) }

        assertThat(monitor.getMessages()[0]).isEqualTo(
            "New registration{nickname='%s', email='%s'}\nvia %s".format(
                ProfileControllerTest.USER_NAME, ProfileControllerTest.USER_EMAIL, USER_AGENT
            )
        )
        assertNewPasswordEmail()
    }


    @Test
    fun registerNewUserWithPassword() {
        val newUser = createNewUser()
        newUser.newPassword = "verySecretPassword"

        sut.register(newUser, USER_AGENT)

        verify { userDao.findByNormalizedName(ProfileControllerTest.USER_NAME) }
        verify { userDao.countBlockedUsername(ProfileControllerTest.USER_NAME) }
        verify { userDao.findByEmail(ProfileControllerTest.USER_EMAIL) }
        verify { userDao.insert(any(), any(), any()) }
        verify(exactly = 0) { userDao.updateCredentials(any(), any()) }

        assertThat(monitor.getMessages()[0]).isEqualTo(
            "New registration{nickname='%s', email='%s'}\nvia %s".format(
                ProfileControllerTest.USER_NAME, ProfileControllerTest.USER_EMAIL, USER_AGENT
            )
        )
        assertVerificationEmail(mailer)
    }

    @Test
    fun updateMyProfileNewMail() {
        every { userDao.findByEmail("newname@example.com") } returns null
        val existingUser = givenExistingUser()
        val updatedUser = createNewUser()
        updatedUser.id = existingUser.id
        updatedUser.email = "newname@example.com"

        sut.updateProfile(existingUser, updatedUser, USER_AGENT)

        assertVerificationEmail(mailer)
        val user = givenExistingUser()
        user.email = "newname@example.com"
        verify { userDao.update(user.id, user) }
    }

    @Test
    fun changePasswordTooShortBody() {
        val user = givenExistingUser()

        assertThatThrownBy { sut.changePassword(user, "secret") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun changePasswordBody() {
        val user = givenExistingUser()

        sut.changePassword(user, "secretlong")

        verify { userDao.updateCredentials(eq(user.id), any()) }
        verify { authorizationDao.deleteAllByUser(user.name) }
    }

    @Test
    fun verifyEmailSuccess() {
        val token = "verification"
        val user = createNewUser()
        user.id = ProfileControllerTest.EXISTING_USER_ID
        user.emailVerification = token
        every { userDao.findByEmailVerification(token) } returns user

        sut.emailVerification(token)

        assertThat(monitor.getMessages()[0]).isEqualTo(
            "Email verified {nickname='${ProfileControllerTest.USER_NAME}', email='${ProfileControllerTest.USER_EMAIL}'}"
        )
        verify { userDao.updateEmailVerification(ProfileControllerTest.EXISTING_USER_ID, User.EMAIL_VERIFIED) }
    }

    @Test
    fun verifyEmailFailed() {
        val token = "verification"
        val user = createNewUser()
        user.id = ProfileControllerTest.EXISTING_USER_ID
        user.emailVerification = token
        every { userDao.findByEmailVerification(token) } returns user

        sut.emailVerification("wrong_token")

        assertThat(monitor.getMessages().isEmpty()).isTrue()
        verify(exactly = 0) {
            userDao.updateEmailVerification(
                ProfileControllerTest.EXISTING_USER_ID,
                User.EMAIL_VERIFIED
            )
        }
    }

    @Test
    fun resendEmailVerification() {
        val user = givenExistingUser()

        sut.resendEmailVerification(user)

        assertVerificationEmail(mailer)
        verify { userDao.updateEmailVerification(eq(ProfileControllerTest.EXISTING_USER_ID), any()) }
    }

    @Test
    fun deleteMyProfile() {
        val user = givenExistingUser()

        sut.deleteProfile(user, USER_AGENT)

        verify { userDao.anonymizeUser(ProfileControllerTest.EXISTING_USER_ID) }
        verify { userDao.addUsernameToBlocklist(ProfileControllerTest.USER_NAME) }
        verify { authorizationDao.deleteAllByUser(ProfileControllerTest.USER_NAME) }
    }

    @Test
    fun registerUserNameTaken() {
        val user = givenExistingUser()
        val newUser = createNewUser()
        newUser.name = user.name

        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(ProfileConflictException::class.java)
    }

    @Test
    fun registerUserNameBlocked() {
        val newUser = createNewUser()
        newUser.name = "Blocked Name"
        every { userDao.countBlockedUsername("blockedname") } returns 1

        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(ProfileConflictException::class.java)
    }

    @Test
    fun registerUserEmailTaken() {
        val user = givenExistingUser()
        val newUser = createNewUser()
        newUser.name = "othername"
        newUser.email = user.email

        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(ProfileConflictException::class.java)

        assertThat(monitor.getMessages()[0])
            .isEqualTo("Registration for user 'othername' with eMail 'existing@example.com' failed, eMail is already taken\nvia UserAgent")
    }

    @Test
    fun registernUserNameTaken() {
        val user = givenExistingUser()
        val newUser = createNewUser()
        newUser.name = user.name
        newUser.email = "otheremail@example.com"

        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(ProfileConflictException::class.java)

        assertThat(monitor.getMessages()[0]).isEqualTo(
            "Registration for user '%s' with eMail '%s' failed, name is already taken by different eMail '%s'%nvia %s"
                .format(user.name, newUser.email, user.email, USER_AGENT)
        )
    }

    @Test
    fun registerUserWithEmptyName() {
        val newUser = createNewUser()
        newUser.name = ""

        assertThatThrownBy { sut.register(newUser, USER_AGENT) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun assertNewPasswordEmail() {
        verify(exactly = 1) {
            mailer.send(
                any(),
                any(), match { it ->
                    it.matches(
                        """
                        Hello,
                        
                        your new password is: .*
                        
                        Cheers
                        Your Railway-Stations-Team
                        """.trimIndent().toRegex()
                    )
                })
        }
    }

    private fun createNewUser(): User {
        val key =
            "246172676F6E32696424763D3139246D3D36353533362C743D322C703D3124426D4F637165757646794E44754132726B566A6A3177246A7568362F6E6C2F49437A4B475570446E6B674171754A304F7A486A62694F587442542F2B62584D49476300000000000000000000000000000000000000000000000000000000000000"
        return User(
            name = ProfileControllerTest.USER_NAME,
            url = "https://link@example.com",
            license = License.CC0_10,
            email = ProfileControllerTest.USER_EMAIL,
            ownPhotos = true,
            key = key,
        )
    }

    private fun givenExistingUser(): User {
        val user = createNewUser()
        user.id = ProfileControllerTest.EXISTING_USER_ID
        every { userDao.findByEmail(user.email!!) } returns user
        every { userDao.findByNormalizedName(user.name) } returns user
        return user
    }

    @Test
    fun resetPasswordUnknownUser() {
        assertThatThrownBy { sut.resetPassword("unknown_user", USER_AGENT) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun resetPasswordEmptyEmail() {
        every { userDao.findByNormalizedName(ProfileControllerTest.USER_NAME) } returns createNewUser().copy(email = null)

        assertThatThrownBy { sut.resetPassword(ProfileControllerTest.USER_NAME, USER_AGENT) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun resetPasswordViaUsernameEmailNotVerified() {
        val user = createNewUser()
        user.id = 123
        every { userDao.findByNormalizedName(ProfileControllerTest.USER_NAME) } returns user

        sut.resetPassword(ProfileControllerTest.USER_NAME, USER_AGENT)

        verify { userDao.updateCredentials(eq(123), any()) }
        assertThat(monitor.getMessages()[0]).isEqualTo(
            "Reset Password for '%s', email='%s'".format(
                ProfileControllerTest.USER_NAME, ProfileControllerTest.USER_EMAIL
            )
        )
        assertNewPasswordEmail()
        verify { userDao.updateEmailVerification(123, User.EMAIL_VERIFIED_AT_NEXT_LOGIN) }
        verify { authorizationDao.deleteAllByUser(ProfileControllerTest.USER_NAME) }
    }

    @Test
    fun resetPasswordViaEmailAndEmailVerified() {
        val user = createNewUser()
        user.id = 123
        user.emailVerification = User.EMAIL_VERIFIED
        every { userDao.findByEmail(user.email!!) } returns user

        sut.resetPassword(user.email!!, USER_AGENT)

        verify { userDao.updateCredentials(eq(123), any()) }
        assertThat(monitor.getMessages()[0]).isEqualTo(
            "Reset Password for '%s', email='%s'".format(
                ProfileControllerTest.USER_NAME, ProfileControllerTest.USER_EMAIL
            )
        )
        assertNewPasswordEmail()
        verify(exactly = 0) { userDao.updateEmailVerification(123, User.EMAIL_VERIFIED_AT_NEXT_LOGIN) }
        verify { authorizationDao.deleteAllByUser(user.name) }
    }

    companion object {
        const val USER_AGENT: String = "UserAgent"

        fun assertVerificationEmail(mailer: Mailer) {
            verify(exactly = 0) {
                mailer.send(
                    any(),
                    any(), match {
                        it.matches(
                            """
                                Hello,

                                please click on EMAIL_VERIFICATION_URL.* to verify your eMail-Address.

                                Cheers
                                Your Railway-Stations-Team

                                ---
                                Hallo,

                                bitte klicke auf EMAIL_VERIFICATION_URL.*, um Deine eMail-Adresse zu verifizieren.

                                Viele Grüße
                                Dein Bahnhofsfoto-Team
                                """.trimIndent().toRegex()
                        )
                    }
                )
            }
        }
    }
}