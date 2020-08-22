/**
 *@author Nikolaus Knop
 */

package hextant.settings.model

import bundles.Property
import kotlin.reflect.KClass

/**
 * A property configurable in a settings file.
 * @property property the property that can be configured
 * @property type the type of the property
 */
class ConfigurableProperty(val property: Property<Any?, Any, Any>, val type: KClass<*>)