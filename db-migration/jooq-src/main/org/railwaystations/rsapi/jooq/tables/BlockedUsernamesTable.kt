/*
 * This file is generated by jOOQ.
 */
package org.railwaystations.rsapi.jooq.tables


import java.time.Instant

import kotlin.collections.Collection
import kotlin.collections.List

import org.jooq.Condition
import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.InverseForeignKey
import org.jooq.Name
import org.jooq.PlainSQL
import org.jooq.QueryPart
import org.jooq.Record
import org.jooq.SQL
import org.jooq.Schema
import org.jooq.Select
import org.jooq.Stringly
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.railwaystations.rsapi.jooq.Test
import org.railwaystations.rsapi.jooq.keys.KEY_BLOCKED_USERNAMES_NORMALIZEDNAME
import org.railwaystations.rsapi.jooq.keys.KEY_BLOCKED_USERNAMES_PRIMARY
import org.railwaystations.rsapi.jooq.tables.records.BlockedUsernamesRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class BlockedUsernamesTable(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, BlockedUsernamesRecord>?,
    parentPath: InverseForeignKey<out Record, BlockedUsernamesRecord>?,
    aliased: Table<BlockedUsernamesRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<BlockedUsernamesRecord>(
    alias,
    Test.TEST,
    path,
    childPath,
    parentPath,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table(),
    where,
) {
    companion object {

        /**
         * The reference instance of <code>test.blocked_usernames</code>
         */
        val BlockedUsernamesTable: BlockedUsernamesTable = BlockedUsernamesTable()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<BlockedUsernamesRecord> = BlockedUsernamesRecord::class.java

    /**
     * The column <code>test.blocked_usernames.id</code>.
     */
    val id: TableField<BlockedUsernamesRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>test.blocked_usernames.name</code>.
     */
    val name: TableField<BlockedUsernamesRecord, String?> = createField(DSL.name("name"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>test.blocked_usernames.created_at</code>.
     */
    val createdAt: TableField<BlockedUsernamesRecord, Instant?> = createField(DSL.name("created_at"), SQLDataType.INSTANT.nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp()"), SQLDataType.INSTANT)), this, "")

    private constructor(alias: Name, aliased: Table<BlockedUsernamesRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<BlockedUsernamesRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<BlockedUsernamesRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>test.blocked_usernames</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>test.blocked_usernames</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>test.blocked_usernames</code> table reference
     */
    constructor(): this(DSL.name("blocked_usernames"), null)
    override fun getSchema(): Schema? = if (aliased()) null else Test.TEST
    override fun getIdentity(): Identity<BlockedUsernamesRecord, Long?> = super.getIdentity() as Identity<BlockedUsernamesRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<BlockedUsernamesRecord> = KEY_BLOCKED_USERNAMES_PRIMARY
    override fun getUniqueKeys(): List<UniqueKey<BlockedUsernamesRecord>> = listOf(KEY_BLOCKED_USERNAMES_NORMALIZEDNAME)
    override fun `as`(alias: String): BlockedUsernamesTable = BlockedUsernamesTable(DSL.name(alias), this)
    override fun `as`(alias: Name): BlockedUsernamesTable = BlockedUsernamesTable(alias, this)
    override fun `as`(alias: Table<*>): BlockedUsernamesTable = BlockedUsernamesTable(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): BlockedUsernamesTable = BlockedUsernamesTable(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): BlockedUsernamesTable = BlockedUsernamesTable(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): BlockedUsernamesTable = BlockedUsernamesTable(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): BlockedUsernamesTable = BlockedUsernamesTable(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): BlockedUsernamesTable = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): BlockedUsernamesTable = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): BlockedUsernamesTable = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): BlockedUsernamesTable = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): BlockedUsernamesTable = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): BlockedUsernamesTable = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): BlockedUsernamesTable = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): BlockedUsernamesTable = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): BlockedUsernamesTable = where(DSL.notExists(select))
}
