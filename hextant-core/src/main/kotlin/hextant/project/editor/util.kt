/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.ifOk
import hextant.serial.HextantFileManager
import hextant.serial.now
import reaktive.Observer
import java.nio.file.Path
import java.nio.file.Paths

internal fun ProjectItemEditor<*, *>.getItemNameEditor(): FileNameEditor? = when (this) {
    is FileEditor<*>      -> itemName
    is DirectoryEditor<*> -> itemName
    else                  -> error("Invalid project item editor $this")
}

internal fun ProjectItemEditor<*, *>.getProjectItemEditorParent(): ProjectItemEditor<*, *>? {
    var cur = parent
    while (true) {
        if (cur == null) return null
        if (cur is ProjectItemEditor<*, *>) return cur
        else cur = cur.parent
    }
}

private fun ProjectItemEditor<*, *>.resolve(name: String): Path {
    val p = getProjectItemEditorParent()?.path ?: return Paths.get(name)
    return p.now.resolve(name)
}

internal fun ProjectItemEditor<*, *>.renamePhysicalOnNameChange(): Observer {
    val name = getItemNameEditor() ?: error("Invalid project item editor")
    return name.result.observe { _, old, new ->
        ifOk(old, new) { o, n ->
            context[HextantFileManager].rename(resolve(o), resolve(n))
        }
    }
}
