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
@Target(CLASS, FUNCTION, CONSTRUCTOR)
annotation class Token(
    val classLocation: String = DEFAULT,
    val nodeType: KClass<*> = None::class,
    val register: Boolean = true
)

@Retention(SOURCE)
@Target(CLASS, FUNCTION, CONSTRUCTOR)
annotation class Compound(
    val classLocation: String = DEFAULT,
    val nodeType: KClass<*> = None::class,
    val register: Boolean = true
)

@Retention(SOURCE)
@Target(CLASS)
annotation class NodeType(val nullableResult: Boolean, val interfaceLocation: String = DEFAULT)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditorInterface(val clz: KClass<*>, vararg val delegated: KClass<*>)

@Retention(SOURCE)
@Target(CLASS)
annotation class Expandable(
    val delegator: KClass<*>,
    val expanderLocation: String = DEFAULT,
    val nodeType: KClass<*> = None::class,
    val childContext: String = "context"
)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditableList(
    val classLocation: String = DEFAULT,
    val editorCls: KClass<*> = None::class,
    val childContext: String = "context"
)

@Retention(SOURCE)
@Target(CLASS)
annotation class UseEditor(val cls: KClass<*>)

@Retention(SOURCE)
@Target(VALUE_PARAMETER)
annotation class Component(val editor: KClass<*> = None::class, val childContext: String = "context")

@Retention(SOURCE)
@Target(CLASS)
annotation class RequestAspect(val optional: Boolean = false)

@Retention(SOURCE)
@Target(CLASS)
annotation class ProvideFeature

@Retention(SOURCE)
@Target(CLASS, CONSTRUCTOR, FUNCTION)
annotation class ProvideImplementation(val aspect: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS, CONSTRUCTOR, FUNCTION)
annotation class ProvideProjectType(val name: String)