/**
 *@author Nikolaus Knop
 */

package hextant.settings.model

import hextant.bundle.Property
import kotlin.reflect.KClass

class ConfigurableProperty(val property: Property<Any, Any, Any>, val type: KClass<*>)