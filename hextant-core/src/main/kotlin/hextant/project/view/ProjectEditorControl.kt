/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.fx.*
import hextant.fx.ModifierValue.MAYBE
import hextant.project.editor.*
import hextant.util.DoubleWeakHashMap
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import javafx.scene.control.*
import javafx.scene.control.SelectionMode.MULTIPLE
import javafx.scene.input.KeyCode
import reaktive.Observer
import reaktive.list.ListChange.*
import reaktive.list.ReactiveList
import reaktive.list.binding.ListBinding
import reaktive.list.binding.listBinding
import reaktive.list.unmodifiableReactiveList
import reaktive.value.*
import reaktive.value.binding.map
import kotlin.concurrent.thread

class ProjectEditorControl(private val editor: DirectoryEditor<*>, arguments: Bundle) :
    EditorControl<TreeView<ProjectItemEditor<*, *>>>(editor, arguments) {
    private val items = DoubleWeakHashMap<ProjectItemEditor<*, *>, TreeItem<ProjectItemEditor<*, *>>>()

    override fun createDefaultRoot(): TreeView<ProjectItemEditor<*, *>> = TreeView(createTreeItem(editor)).apply {
        setCellFactory { Cell() }
    }

    private class ObservableListAdapter<E>(private val wrapped: ReactiveList<E>) : ObservableListBase<E>() {
        private val obs = wrapped.observeList { c ->
            beginChange()
            when (c) {
                is Added    -> nextAdd(c.index, c.index + 1)
                is Removed  -> nextRemove(c.index, c.element)
                is Replaced -> nextReplace(c.index, c.index + 1, mutableListOf(c.old))
            }
            endChange()
        }

        override fun get(index: Int): E = wrapped.now[index]

        override val size: Int
            get() = wrapped.now.size
    }

    private inner class Cell : TreeCell<ProjectItemEditor<*, *>>() {
        override fun updateItem(item: ProjectItemEditor<*, *>?, empty: Boolean) {
            super.updateItem(item, empty)
            graphic = item.takeUnless { empty }?.let {
                context.createView(it)
            }
        }
    }

    private inner class DirectoryTreeItem(
        value: ProjectItemEditor<*, *>,
        editors: ReactiveList<ProjectItemEditor<*, *>>
    ) : TreeItem<ProjectItemEditor<*, *>>(value) {
        private val obs: Observer

        init {
            editors.now.forEach { e ->
                children.add(createTreeItem(e))
            }
            obs = editors.observeList { ch ->
                when (ch) {
                    is Removed  -> children.removeAt(ch.index)
                    is Added    -> children.add(ch.index, createTreeItem(ch.element))
                    is Replaced -> children[ch.index] = createTreeItem(ch.new)
                }
            }
        }
    }

    init {
        root.selectionModel.selectionMode = MULTIPLE
        root.registerShortcuts {
            on(shortcut(KeyCode.INSERT) { control(MAYBE) }) {
                val e = root.selectionModel.selectedItem.value
                addNewItem(e)
            }
            @Suppress("UNCHECKED_CAST") //TODO maybe this can be done more elegantly
            on(shortcut(KeyCode.DELETE) { control(MAYBE) }) {
                for (item in root.selectionModel.selectedItems) {
                    val e = item.value
                    val list = e.parent.now
                    if (list !is ProjectItemListEditor<*>) return@on
                    list as ProjectItemListEditor<Any>
                    list.remove(e as ProjectItemEditor<Any, *>)
                    val dir = list.parent.now
                    if (dir !is DirectoryEditor<*>) return@on
                    val v = context[EditorControlGroup].getViewOf(dir)
                    v.requestFocus()
                }
            }
        }
    }

    private fun addNewItem(e: ProjectItemEditor<*, *>?): Boolean {
        when (e) {
            null                   -> return false
            is DirectoryEditor     -> {
                val exp = e.expander.now
                if (exp is ProjectItemExpander<*>) {
                    items[exp]?.isExpanded = true
                }
                addItemTo(e.items)
            }
            is FileEditor          -> {
                val p = e.parent.now as? ProjectItemListEditor<*> ?: return false
                addItemTo(p)
            }
            is ProjectItemExpander -> if (!addNewItem(e.editor.now)) {
                val p = e.parent.now as? ProjectItemListEditor<*> ?: return false
                addItemTo(p)
            }
        }
        return true
    }

    private fun addItemTo(p: ProjectItemListEditor<*>) {
        val new = p.addLast()
        thread {
            //TODO invent slightly less horrific hack
            Thread.sleep(20) //EVIL! Waits for new view to rendered and added to view group.
            Platform.runLater {
                val view = context[EditorControlGroup].getViewOf(new)
                view.receiveFocus()
            }
        }
    }

    private fun createTreeItem(e: ProjectItemEditor<*, *>): TreeItem<ProjectItemEditor<*, *>> {
        val item: TreeItem<ProjectItemEditor<*, *>> = when (e) {
            is FileEditor          -> TreeItem(e)
            is DirectoryEditor     -> DirectoryTreeItem(e, e.items.editors)
            is ProjectItemExpander -> {
                val editors = e.editor.map {
                    if (it is DirectoryEditor) it.items.editors else unmodifiableReactiveList()
                }.flatten()
                DirectoryTreeItem(e, editors)
            }
            else                   -> throw AssertionError("Unexpected project item editor $e")
        }
        items[e] = item
        return item
    }

    companion object {

        private fun <E> ReactiveValue<ReactiveList<E>>.flatten(): ListBinding<E> = listBinding(now.now) {
            var obs: Observer? = null
            val o = forEach { l ->
                if (obs != null) {
                    obs!!.kill()
                    clear()
                    addAll(l.now)
                }
                obs = l.observeList { ch ->
                    when (ch) {
                        is Removed  -> removeAt(ch.index)
                        is Added    -> add(ch.index, ch.element)
                        is Replaced -> set(ch.index, ch.new)
                    }
                }
                addObserver(obs!!)
            }
            addObserver(o)
        }

        private fun <E> ObservableList<E>.bind(other: ReactiveList<E>): Observer {
            setAll(other.now)
            return other.observeList { ch ->
                when (ch) {
                    is Removed  -> removeAt(ch.index)
                    is Added    -> add(ch.index, ch.element)
                    is Replaced -> set(ch.index, ch.new)
                }
            }
        }
    }
}