/**
 * @author Nikolaus Knop
 */

package hextant.bundle

fun <T : Any, P : Permission> Bundle.getOrNull(permission: P, property: Property<T, P, *>): T? =
    try {
        get(permission, property)
    } catch (ex: NoSuchPropertyException) {
        null
    }