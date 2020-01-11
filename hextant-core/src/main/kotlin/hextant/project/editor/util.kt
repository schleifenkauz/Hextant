/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import hextant.get
import hextant.ifOk
import hextant.serial.HextantFileManager
import hextant.serial.now
import reaktive.Observer
import reaktive.value.now
import java.nio.file.Path
import java.nio.file.Paths

fun ProjectItemEditor<*, *>.getItemNameEditor(): FileNameEditor? = when (this) {
    is ProjectItemExpander<*> -> editor.now?.getItemNameEditor()
    is FileEditor<*>          -> fileName
    is DirectoryEditor<*>     -> directoryName
    else                      -> error("Invalid project item editor $this")
}

fun ProjectItemEditor<*, *>.getProjectItemEditorParent(): ProjectItemEditor<*, *>? {
    var cur = parent.now
    while (true) {
        if (cur == null) return null
        if (cur is ProjectItemEditor<*, *>) return cur
        else cur = cur.parent.now
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
