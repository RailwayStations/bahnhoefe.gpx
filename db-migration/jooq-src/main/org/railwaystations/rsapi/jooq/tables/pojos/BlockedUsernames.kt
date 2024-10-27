/*
 * This file is generated by jOOQ.
 */
package org.railwaystations.rsapi.jooq.tables.pojos


import java.io.Serializable
import java.time.Instant


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class BlockedUsernames(
    val id: Long? = null,
    val name: String? = null,
    val createdAt: Instant? = null
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: BlockedUsernames = other as BlockedUsernames
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.name == null) {
            if (o.name != null)
                return false
        }
        else if (this.name != o.name)
            return false
        if (this.createdAt == null) {
            if (o.createdAt != null)
                return false
        }
        else if (this.createdAt != o.createdAt)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + (if (this.name == null) 0 else this.name.hashCode())
        result = prime * result + (if (this.createdAt == null) 0 else this.createdAt.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("BlockedUsernames (")

        sb.append(id)
        sb.append(", ").append(name)
        sb.append(", ").append(createdAt)

        sb.append(")")
        return sb.toString()
    }
}
