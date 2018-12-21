package hextant

import hextant.bundle.Property
import hextant.core.CorePermissions.Internal
import hextant.core.CorePermissions.Public

/**
 * Represents a version of the Hextant platform
 * @constructor
 * @property big in 1.234 it would be 1
 * @property iteration in 1.234 it would be 234
 * @property isSnapshot specifies whether this version is a snapshot version
 */
//TODO(find better name for big)
class Version internal constructor(val big: Int, val iteration: Int, val isSnapshot: Boolean) : Comparable<Version> {
    /**
     * Decide whether this or the [other] Version is later
     * Version(1, 345) > Version(1, 23)
     * Version(2, 345) < Version(5, 1)
     * Version(2, 345, isSnapshot = true) < Version(2, 345, isSnapshot = false)
     */
    override fun compareTo(other: Version): Int {
        return when {
            big != other.big                -> big - other.big
            iteration != other.iteration    -> iteration - other.iteration
            isSnapshot && !other.isSnapshot -> -1
            else                            -> 1
        }
    }

    /**
     * @return `true` if the [big] version number the [iteration] are the same and both have the same [isSnapshot] flag
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Version

        if (big != other.big) return false
        if (iteration != other.iteration) return false
        if (isSnapshot != other.isSnapshot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = big
        result = 31 * result + iteration
        result = 31 * result + isSnapshot.hashCode()
        return result
    }

    /**
     * The version property
     */
    companion object : Property<Version, Public, Internal>("version")
}
