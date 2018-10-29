/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.ConvertedEditable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.core.view.FXExpanderView
import org.nikok.hextant.prop.Property
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

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

    @Suppress("UNCHECKED_CAST") private class Impl : EditorViewFactory {
        private val viewFactories = ClassMap.invariant<(Editable<*>) -> FXEditorView>()

        override fun <E : Editable<*>> registerFX(editableCls: KClass<out E>, viewFactory: (E) -> FXEditorView) {
            @Suppress("UNCHECKED_CAST") val cast = viewFactory as (Editable<*>) -> FXEditorView
            viewFactories[editableCls] = cast
        }

        override fun <E : Editable<*>> getFXView(editable: E): FXEditorView {
            val cls = editable::class
            when (editable) {
                is ConvertedEditable<*, *> -> return getFXView(editable.source)
                is Expandable<*, *>        -> return expanderView(editable, cls) ?: unresolvedView(cls)
                else                       -> {
                    viewFactories[cls]?.let { f -> return f(editable) }
                    createDefault(editable, cls)?.let { return it }
                    unresolvedView(cls)
                }
            }
        }

        private fun <E : Editable<*>> unresolvedView(cls: KClass<out E>): Nothing {
            throw NoSuchElementException("Could not resolve view for $cls")
        }

        private fun <E : Any> createDefault(editable: E, editableCls: KClass<out E>): FXEditorView? {
            val viewCls = resolveDefault(editableCls) ?: return null
            val constructor = viewCls.constructors.find { c ->
                val editableParameter = c.parameters.first()
                c.parameters.count { p -> !p.isOptional } == 1 &&
                !editableParameter.isOptional &&
                editableParameter.type.classifier == editableCls
            } ?: throw NoSuchElementException("Could not find constructor for $viewCls")
            return constructor.callBy(mapOf(constructor.parameters.first() to editable))
        }

        private fun expanderView(expandable: Expandable<*, *>, expandableCls: KClass<*>): FXExpanderView? {
/*            val expanderCls = expanderCls(expandableCls, expandable)
            expanderCls ?: return null
            expandable as Expandable<*, Editable<*>>
            val expander: Expander<*> = createExpander(expanderCls, expandable, expandableCls)*/
            return FXExpanderView(expandable)
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
            return tryCreateViewCls(inSamePackage) ?:
                   tryCreateViewCls(inViewPackage) ?:
                   tryCreateViewCls(inSiblingViewPkg)
        }

        private fun tryCreateViewCls(name: String): KClass<FXEditorView>? {
            return try {
                val cls = Class.forName(name)
                val k = cls.kotlin
                k.takeIf { it.isSubclassOf(FXEditorView::class) } as KClass<FXEditorView>?
            } catch (cnf: ClassNotFoundException) {
                null
            }
        }
    }

    companion object: Property<EditorViewFactory, Public, Internal>("editor-view-factory") {
        fun newInstance(): EditorViewFactory = Impl()

        inline fun newInstance(configure: EditorViewFactory.() -> Unit): EditorViewFactory =
                newInstance().apply(configure)
    }
}

inline fun <reified E : Editable<*>> EditorViewFactory.registerFX(noinline viewFactory: (E) -> FXEditorView) {
    registerFX(E::class, viewFactory)
}

inline fun EditorViewFactory.configure(config: EditorViewFactory.() -> Unit) {
    apply(config)
}