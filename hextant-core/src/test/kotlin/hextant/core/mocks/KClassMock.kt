/**
 *@author Nikolaus Knop
 */

package hextant.core.mocks

import kotlin.reflect.*
import kotlin.reflect.KVisibility.PUBLIC

internal abstract class KClassMock : KClass<Nothing> {
    override val annotations: List<Annotation>
        get() = emptyList()
    override val constructors: Collection<KFunction<Nothing>>
        get() = emptyList()
    override val isAbstract: Boolean
        get() = false
    override val isCompanion: Boolean
        get() = false
    override val isData: Boolean
        get() = false
    override val isFinal: Boolean
        get() = false
    override val isInner: Boolean
        get() = false
    override val isOpen: Boolean
        get() = true
    override val isSealed: Boolean
        get() = false
    override val members: Collection<KCallable<*>>
        get() = emptyList()
    override val nestedClasses: Collection<KClass<*>>
        get() = emptyList()
    override val objectInstance: Nothing?
        get() = null
    override val simpleName: String?
        get() = qualifiedName?.substringAfterLast(".")
    override val supertypes: List<KType>
        get() = emptyList()
    override val typeParameters: List<KTypeParameter>
        get() = emptyList()
    override val visibility: KVisibility?
        get() = PUBLIC
    override val sealedSubclasses: List<KClass<out Nothing>>
        get() = emptyList()
    override val isFun: Boolean
        get() = false

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int {
        return qualifiedName?.hashCode() ?: 0
    }

    override fun isInstance(value: Any?): Boolean {
        return if (value == null) false
        else value::class.qualifiedName == this.qualifiedName
    }

    companion object {
        fun withQualifiedName(name: String): KClassMock = object : KClassMock() {
            override val qualifiedName: String = name
        }
    }
}