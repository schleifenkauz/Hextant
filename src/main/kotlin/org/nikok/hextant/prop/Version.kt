package org.nikok.hextant.prop

class Version private constructor(val big: Int, val iteration: Int, val isSnapshot: Boolean): Comparable<Version> {
    override fun compareTo(other: Version): Int {
        return when {
            big != other.big                -> big - other.big
            iteration != other.iteration    -> iteration - other.iteration
            isSnapshot && !other.isSnapshot -> -1
            else                            -> 1
        }
    }

    companion object {
        val current: Version = Version(1, 0, isSnapshot = true)
    }

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
}
