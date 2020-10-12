/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.serial.*
import kserial.*
import reaktive.collection.ReactiveCollection
import reaktive.list.reactiveList
import reaktive.value.*

/**
 * Skeletal implementation for [Editor]s
 */
@Suppress("OverridingDeprecatedMember")
abstract class AbstractEditor<out R, in V : Any>(
    override val context: Context
) : Editor<R>, AbstractController<V>(), Serializable {
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

    override fun serialize(output: Output) {
        output.writeObject(snapshot())
    }

    override fun deserialize(input: Input) {
        val snapshot = input.readTyped<EditorSnapshot<AbstractEditor<*, *>>>()
        snapshot.reconstruct(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun paste(snapshot: EditorSnapshot<*>): Boolean {
        val cls = snapshot.getTypeArgument(EditorSnapshot::class, 0)
        if (!cls.isInstance(this)) return false
        snapshot as EditorSnapshot<Editor<*>>
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