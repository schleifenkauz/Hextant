/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import hextant.serial.Snapshot
import hextant.serial.VirtualFile
import reaktive.collection.ReactiveCollection
import reaktive.list.reactiveList
import reaktive.value.ReactiveValue
import reaktive.value.now
import reaktive.value.reactiveVariable
import kotlin.reflect.KClass

/**
 * Skeletal implementation for [Editor]s
 */
@Suppress("OverridingDeprecatedMember")
abstract class AbstractEditor<out R, in V : Any>(override val context: Context) : Editor<R>, AbstractController<V>() {
    final override var parent: Editor<*>? = null
        private set

    private val _children = reactiveList<Editor<*>>()

    override val children: ReactiveCollection<Editor<*>> get() = _children

    final override var expander: Expander<*, *>? = null
        private set

    override fun initParent(parent: Editor<*>) {
        this.parent = parent
        onInitParent(parent)
    }

    override fun initExpander(expander: Expander<@UnsafeVariance R, *>) {
        this.expander = expander
    }

    private val _accessor = reactiveVariable<EditorAccessor?>(null)

    final override val accessor: ReactiveValue<EditorAccessor?> get() = _accessor

    override fun setAccessor(acc: EditorAccessor) {
        _accessor.now = acc
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        throw InvalidAccessorException(accessor)
    }

    /**
     * Makes the [editor] a child of this editor and just returns it
     */
    protected fun <E : Editor<*>> addChild(editor: E): E {
        @Suppress("DEPRECATION")
        editor.initParent(this)
        _children.now.add(editor)
        return editor
    }

    /**
     * Removes the given [editor] from the [children].
     * @throws IllegalStateException if [editor] is not a child of this editor.
     */
    protected fun <E : Editor<*>> removeChild(editor: E) {
        if (!_children.now.remove(editor)) throw IllegalStateException("$editor is not a child of $this")
    }

    /**
     * Make all the given editors children of this editor
     */
    protected fun children(vararg children: Editor<*>) {
        for (c in children) addChild(c)
    }

    private var _file: VirtualFile<Editor<*>>? = null

    override fun setFile(file: VirtualFile<Editor<*>>) {
        _file = file
    }

    override val file: VirtualFile<Editor<*>>?
        get() = _file ?: parent?.file ?: expander?.file

    override val isRoot: Boolean
        get() = _file != null

    /**
     * Returns the [KClass] instance for the class that is used to save and reconstruct the state of this editor type.
     *
     * The default implementation uses [createSnapshot] to create a snapshot and then returns
     * the runtime class of the returned object. This may be non-desirable if the constructor of
     * the snapshot class does any non-trivial work. In this case this method should be overridden
     * to directly return the runtime class of objects that would be returned by [createSnapshot].
     */
    protected open fun snapshotClass(): KClass<out Snapshot<*>> = createSnapshot()::class

    @Suppress("UNCHECKED_CAST")
    override fun paste(snapshot: Snapshot<out Editor<*>>): Boolean {
        if (!snapshotClass().isInstance(snapshot)) return false
        snapshot as Snapshot<Editor<*>>
        snapshot.reconstruct(this)
        return true
    }

    override fun viewAdded(view: V) {
        context.executeSafely("adding view", Unit) { super.viewAdded(view) }
    }

    override fun views(action: (@UnsafeVariance V).() -> Unit) {
        super.views { context.executeSafely("notify views", Unit) { action() } }
    }
}