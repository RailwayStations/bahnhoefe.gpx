package org.railwaystations.rsapi.adapter.db

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.railwaystations.rsapi.core.model.User
import org.railwaystations.rsapi.core.model.nameToLicense
import org.railwaystations.rsapi.core.ports.outbound.UserPort
import java.sql.ResultSet
import java.util.*

interface UserDao : UserPort {
    @SqlQuery("SELECT * FROM \"user\"")
    @RegisterRowMapper(UserMapper::class)
    override fun list(): List<User>

    @SqlQuery("SELECT * FROM \"user\" WHERE name = :name")
    @RegisterRowMapper(
        UserMapper::class
    )
    override fun findByName(@Bind("name") name: String): User?

    @SqlQuery("SELECT * FROM \"user\" WHERE id = :id")
    @RegisterRowMapper(UserMapper::class)
    override fun findById(@Bind("id") id: Long): User?

    @SqlQuery("SELECT * FROM \"user\" WHERE email = :email")
    @RegisterRowMapper(UserMapper::class)
    override fun findByEmail(@Bind("email") email: String): User?

    @SqlUpdate("UPDATE \"user\" SET \"key\" = :key WHERE id = :id")
    override fun updateCredentials(@Bind("id") id: Long, @Bind("key") key: String)

    @SqlUpdate(
        """
            INSERT INTO "user" (id, name, url, license, email, ownPhotos, anonymous, \"key\", emailVerification, sendNotifications, locale)
                VALUES (:id, :name, :url, :license, :email, :ownPhotos, :anonymous, :key, :emailVerification, :sendNotifications, :localeLanguageTag)
            
            """
    )
    @GetGeneratedKeys("id")
    override fun insert(
        @BindBean user: User,
        @Bind("key") key: String?,
        @Bind("emailVerification") emailVerification: String?
    ): Long

    @SqlUpdate(
        """
            UPDATE "user" SET name = :name, url = :url, license = :license, email = :email, ownPhotos = :ownPhotos,
                            anonymous = :anonymous, sendNotifications = :sendNotifications, locale = :localeLanguageTag
            WHERE id = :id
            
            """
    )
    override fun update(@Bind("id") id: Long, @BindBean user: User)

    @SqlQuery("SELECT * FROM \"user\" WHERE emailVerification = :emailVerification")
    @RegisterRowMapper(
        UserMapper::class
    )
    override fun findByEmailVerification(@Bind("emailVerification") emailVerification: String): User?

    @SqlUpdate("UPDATE \"user\" SET emailVerification = :emailVerification WHERE id = :id")
    override fun updateEmailVerification(@Bind("id") id: Long, @Bind("emailVerification") emailVerification: String?)

    @SqlUpdate(
        """
            UPDATE "user"
             SET name = CONCAT('deleteduser', id),
                 url = NULL,
                 anonymous = true,
                 email = NULL,
                 emailVerification = NULL,
                 ownPhotos = false,
                 license = NULL,
                 \"key\" = NULL,
                 sendNotifications = false,
                 admin = false
             WHERE id = :id
            
            """
    )
    override fun anonymizeUser(@Bind("id") id: Long)

    @SqlUpdate(
        """
            INSERT INTO blocked_username (name)
                VALUES (:name)
            
            """
    )
    override fun addUsernameToBlocklist(@Bind("name") name: String)

    @SqlQuery("SELECT COUNT(*) FROM blocked_username WHERE name = :name")
    override fun countBlockedUsername(@Bind("name") name: String): Int

    @SqlUpdate("UPDATE \"user\" SET locale = :localeLanguageTag WHERE id = :id")
    override fun updateLocale(@Bind("id") id: Long, @Bind("localeLanguageTag") locallocaleLanguageTage: String)

    class UserMapper : RowMapper<User> {
        override fun map(rs: ResultSet, ctx: StatementContext): User {
            val locale = rs.getString("locale")
            return User(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                url = rs.getString("url"),
                license = rs.getString("license").nameToLicense(),
                email = rs.getString("email"),
                ownPhotos = rs.getBoolean("ownPhotos"),
                anonymous = rs.getBoolean("anonymous"),
                key = rs.getString("key"),
                admin = rs.getBoolean("admin"),
                emailVerification = rs.getString("emailVerification"),
                newPassword = null,
                sendNotifications = rs.getBoolean("sendNotifications"),
                locale = if (locale != null) Locale.forLanguageTag(locale) else Locale.ENGLISH
            )
        }
    }
}
