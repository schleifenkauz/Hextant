/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

const val DEFAULT = "<default>"

sealed class None

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
    val delegator: KClass<*>,
    val expanderLocation: String = DEFAULT,
    val subtypeOf: KClass<*> = None::class
)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditableList(val classLocation: String = DEFAULT, val editorCls: KClass<*> = None::class)

@Retention(SOURCE)
@Target(VALUE_PARAMETER, CLASS)
annotation class UseEditor(val cls: KClass<*>)

@Retention(SOURCE)
@Target(CLASS)
annotation class RequestAspect(val optional: Boolean = false)

@Retention(SOURCE)
@Target(CLASS)
annotation class ProvideFeature

@Retention(SOURCE)
@Target(CLASS, CONSTRUCTOR, FUNCTION)
annotation class ProvideImplementation(val aspect: KClass<*> = None::class, val feature: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class ProvideProjectType(val name: String)