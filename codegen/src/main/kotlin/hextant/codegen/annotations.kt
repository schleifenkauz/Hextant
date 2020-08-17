/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import hextant.core.Editor
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

const val DEFAULT = "<default>"

internal sealed class None

@Retention(SOURCE)
@Target(CLASS)
annotation class Token(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Compound(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Alternative(val interfaceLocation: String = DEFAULT)

@Retention(SOURCE)
@Target(CLASS)
annotation class Expandable(
    val delegator: KClass<out ExpanderDelegator<*>>,
    val expanderLocation: String = DEFAULT,
    val subtypeOf: KClass<*> = None::class
)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditableList(val classLocation: String = DEFAULT, val editorCls: KClass<*> = None::class)

@Retention(SOURCE)
@Target(VALUE_PARAMETER, CLASS)
annotation class UseEditor(val cls: KClass<out Editor<*>>)

@Retention(SOURCE)
@Target(CLASS)
annotation class RequestAspect(val optional: Boolean = false)

@Retention(SOURCE)
@Target(CLASS)
annotation class RequestFeature

@Retention(SOURCE)
@Target(CLASS)
annotation class ProvideImplementation

@Retention(SOURCE)
@Target(CLASS)
annotation class ProvideProjectType(val name: String)