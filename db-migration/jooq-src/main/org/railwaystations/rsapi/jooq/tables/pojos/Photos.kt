/*
 * This file is generated by jOOQ.
 */
package org.railwaystations.rsapi.jooq.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class Photos(
    val id: Long? = null,
    val countrycode: String,
    val stationid: String,
    val primary: Boolean? = null,
    val outdated: Boolean? = null,
    val urlpath: String,
    val license: String,
    val photographerid: Int,
    val createdat: String
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: Photos = other as Photos
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.countrycode != o.countrycode)
            return false
        if (this.stationid != o.stationid)
            return false
        if (this.primary == null) {
            if (o.primary != null)
                return false
        }
        else if (this.primary != o.primary)
            return false
        if (this.outdated == null) {
            if (o.outdated != null)
                return false
        }
        else if (this.outdated != o.outdated)
            return false
        if (this.urlpath != o.urlpath)
            return false
        if (this.license != o.license)
            return false
        if (this.photographerid != o.photographerid)
            return false
        if (this.createdat != o.createdat)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.countrycode.hashCode()
        result = prime * result + this.stationid.hashCode()
        result = prime * result + (if (this.primary == null) 0 else this.primary.hashCode())
        result = prime * result + (if (this.outdated == null) 0 else this.outdated.hashCode())
        result = prime * result + this.urlpath.hashCode()
        result = prime * result + this.license.hashCode()
        result = prime * result + this.photographerid.hashCode()
        result = prime * result + this.createdat.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("Photos (")

        sb.append(id)
        sb.append(", ").append(countrycode)
        sb.append(", ").append(stationid)
        sb.append(", ").append(primary)
        sb.append(", ").append(outdated)
        sb.append(", ").append(urlpath)
        sb.append(", ").append(license)
        sb.append(", ").append(photographerid)
        sb.append(", ").append(createdat)

        sb.append(")")
        return sb.toString()
    }
}
