@file: Suppress("UNCHECKED_CAST")

/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.Bundle
import hextant.context.*
import hextant.context.ClipboardContent.OneEditor
import hextant.fx.EditorControl
import hextant.fx.registerShortcuts
import hextant.project.editor.*
import javafx.scene.control.*
import javafx.scene.control.SelectionMode.MULTIPLE
import kollektion.DoubleWeakHashMap
import reaktive.Observer
import reaktive.list.ListChange.*
import reaktive.list.ReactiveList
import kotlin.collections.set

/**
 * Displays a project with the given item-editor as the root.
 */
class ProjectEditorControl(private val editor: ProjectItemEditor<*, *>, arguments: Bundle) :
    EditorControl<TreeView<ProjectItemEditor<*, *>>>(editor, arguments) {
    private val items = DoubleWeakHashMap<ProjectItemEditor<*, *>, TreeItem<ProjectItemEditor<*, *>>>()

    override fun createDefaultRoot(): TreeView<ProjectItemEditor<*, *>> = TreeView(createTreeItem(editor)).apply {
        setCellFactory { Cell() }
    }

    private inner class Cell : TreeCell<ProjectItemEditor<*, *>>() {
        override fun updateItem(item: ProjectItemEditor<*, *>?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                graphic = null
            } else {
                val v = context.createView(item)
                graphic = v
                v.receiveFocus()
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
                    is Added    -> children.add(ch.index, createTreeItem(ch.added))
                    is Replaced -> children[ch.index] = createTreeItem(ch.added)
                }
            }
        }
    }

    init {
        root.selectionModel.selectionMode = MULTIPLE
        root.registerShortcuts {
            on("Ctrl?+F") {
                insertEditor(FileEditor.newInstance(context))
            }
            on("Ctrl?+D") {
                insertEditor(DirectoryEditor(context))
            }
            on("Delete") {
                val selected = root.selectionModel.selectedItems.toList()
                deleteItems(selected)
            }
            on("F2") {
                val item = selectedEditor()
                if (item != null) startRename(item)
            }
            on("Ctrl+Shift+C") {
                val item = selectedEditor() ?: return@on
                context[Clipboard].copy(OneEditor(item.snapshot()))
            }
            on("Ctrl+Shift+V") {
                val selected = selectedEditor() ?: return@on
                val content = context[Clipboard].get()
                if (content is OneEditor) {
                    val copy =
                        context.executeSafely("pasting", null) { content.snapshot.reconstruct(context) } ?: return@on
                    addNewItem(selected, copy as ProjectItemEditor<*, *>)
                }
            }
        }
    }

    private fun insertEditor(new: ProjectItemEditor<Any?, *>) {
        val e = selectedEditor()
        if (e != null) {
            addNewItem(e, new)
        }
    }

    private fun selectedEditor(): ProjectItemEditor<*, *>? = root.selectionModel.selectedItem.value

    private fun startRename(item: ProjectItemEditor<*, *>) {
        val name = item.itemName
        name.beginChange()
    }

    private fun deleteItems(selected: List<TreeItem<ProjectItemEditor<*, *>>>) {
        for (item in selected) {
            val e = item.value
            val list = e.parent
            if (list !is ProjectItemListEditor<*>) return
            list as ProjectItemListEditor<Any?>
            list.remove(e as ProjectItemEditor<Any?, *>, undoable = false)
            val dir = list.parent
            if (dir !is DirectoryEditor<*>) return
            val v = context[EditorControlGroup].getViewOf(dir)
            v.requestFocus()
        }
    }

    private fun addNewItem(
        e: ProjectItemEditor<*, *>?,
        new: ProjectItemEditor<*, *>
    ): Boolean {
        when (e) {
            null               -> return false
            is DirectoryEditor -> {
                addItemTo(e.items as ProjectItemListEditor<Any?>, new)
                items[e]!!.isExpanded = true
            }
            is FileEditor      -> {
                val p = e.parent as? ProjectItemListEditor<*> ?: return false
                addItemTo(p as ProjectItemListEditor<Any?>, new)
            }
        }
        return true
    }

    private fun addItemTo(
        p: ProjectItemListEditor<Any?>,
        new: ProjectItemEditor<*, *>
    ) {
        p.addLast(new as ProjectItemEditor<Any?, *>, undoable = false)
        val item = items[new] ?: error("Did not find tree item associated with $new")
        root.selectionModel.clearSelection()
        root.selectionModel.select(item)
    }

    private fun createTreeItem(e: ProjectItemEditor<*, *>): TreeItem<ProjectItemEditor<*, *>> {
        val item: TreeItem<ProjectItemEditor<*, *>> = when (e) {
            is FileEditor      -> TreeItem(e)
            is DirectoryEditor -> DirectoryTreeItem(e, e.items.editors)
            else               -> throw AssertionError("Unexpected project item editor $e")
        }
        items[e] = item
        return item
    }
}