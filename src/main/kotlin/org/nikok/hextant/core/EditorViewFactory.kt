/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.ConvertedEditable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.core.view.FXExpanderView
import org.nikok.hextant.prop.Property
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

/**
 * Used to manage the views of [Editable]s
 */
interface EditorViewFactory {
    /**
     * Register the specified [viewFactory] to the given [editableCls].
     * From now all calls of [getFXView] with an argument of type [E] will use the [viewFactory]
     */
    fun <E : Editable<*>> registerFX(editableCls: KClass<out E>, viewFactory: (E) -> FXEditorView)

    /**
     * @return the [FXEditorView] associated with the type of the specified [editable]
     * @throws NoSuchElementException if there is no [FXEditorView] registered with this [editable]
     */
    fun <E : Editable<*>> getFXView(editable: E): FXEditorView

    @Suppress("UNCHECKED_CAST") private class Impl(
        private val platform: HextantPlatform,
        private val classLoader: ClassLoader
    ) : EditorViewFactory {
        private val viewFactories = ClassMap.invariant<(Editable<*>) -> FXEditorView>()

        override fun <E : Editable<*>> registerFX(editableCls: KClass<out E>, viewFactory: (E) -> FXEditorView) {
            @Suppress("UNCHECKED_CAST") val cast = viewFactory as (Editable<*>) -> FXEditorView
            viewFactories[editableCls] = cast
        }

        override fun <E : Editable<*>> getFXView(editable: E): FXEditorView {
            val cls = editable::class
            when (editable) {
                is ConvertedEditable<*, *> -> return getFXView(editable.source)
                is Expandable<*, *>        -> return FXExpanderView(editable, platform)
                else                       -> {
                    viewFactories[cls]?.let { f -> return f(editable) }
                    defaultFactory(cls)?.let { c -> return c(editable) }
                    unresolvedView(cls)
                }
            }
        }

        private fun <E : Editable<*>> unresolvedView(cls: KClass<out E>): Nothing {
            throw NoSuchElementException("Could not resolve view for $cls")
        }

        private fun defaultFactory(cls: KClass<out Editable<*>>): ((Editable<*>) -> FXEditorView)? {
            val viewCls = resolveDefault(cls) ?: return null
            val constructor = resolveConstructor(cls, viewCls)
            registerFX(cls, constructor)
            return constructor
        }

        private fun <E : Any> resolveConstructor(
            editableCls: KClass<out E>,
            viewCls: KClass<FXEditorView>
        ): (Editable<*>) -> FXEditorView {
            lateinit var platformParameter: KParameter
            lateinit var editableParameter: KParameter
            val constructor = viewCls.constructors.find { constructor ->
                val parameters = constructor.parameters
                platformParameter = parameters.find {
                    it.type.classifier == HextantPlatform::class
                } ?: return@find false
                editableParameter = parameters.find {
                    it.type.isSupertypeOf(editableCls.starProjectedType)
                } ?: return@find false
                val otherParameters = parameters - setOf(platformParameter, editableParameter)
                otherParameters.count { !it.isOptional } == 0
            } ?: throw java.util.NoSuchElementException("Could not find constructor for $viewCls")
            return { expandable ->
                constructor.callBy(
                    mapOf(
                        editableParameter to expandable,
                        platformParameter to platform
                    )
                )
            }

        }

        private fun resolveDefault(editableCls: KClass<*>): KClass<FXEditorView>? {
            val name = editableCls.simpleName ?: return null
            val pkg = editableCls.java.`package`?.name ?: return null
            if (!name.startsWith("Editable")) return null
            val viewClsName = "FX" + name.removePrefix("Editable") + "EditorView"
            val inSamePackage = "$pkg.$viewClsName"
            val inViewPackage = "$pkg.view.$viewClsName"
            val siblingViewPkg = pkg.replaceAfterLast('.', "view")
            val inSiblingViewPkg = "$siblingViewPkg.$viewClsName"
            return tryCreateViewCls(inSamePackage) ?: tryCreateViewCls(inViewPackage) ?: tryCreateViewCls(
                inSiblingViewPkg
            )
        }

        private fun tryCreateViewCls(name: String): KClass<FXEditorView>? {
            return try {
                val cls = classLoader.loadClass(name)
                val k = cls.kotlin
                k.takeIf { it.isSubclassOf(FXEditorView::class) } as KClass<FXEditorView>?
            } catch (cnf: ClassNotFoundException) {
                null
            }
        }
    }

    companion object : Property<EditorViewFactory, Public, Internal>("editor-view-factory") {
        fun newInstance(platform: HextantPlatform, classLoader: ClassLoader): EditorViewFactory =
            Impl(platform, classLoader)

        inline fun newInstance(
            platform: HextantPlatform,
            configure: EditorViewFactory.() -> Unit,
            classLoader: ClassLoader
        ): EditorViewFactory =
            newInstance(platform, classLoader).apply(configure)
    }
}

inline fun <reified E : Editable<*>> EditorViewFactory.registerFX(noinline viewFactory: (E) -> FXEditorView) {
    registerFX(E::class, viewFactory)
}

inline fun EditorViewFactory.configure(config: EditorViewFactory.() -> Unit) {
    apply(config)
}