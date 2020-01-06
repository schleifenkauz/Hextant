/**
 * @author Nikolaus Knop
 */

package hextant.project.editor

import reaktive.value.now

fun ProjectItemEditor<*, *>.getItemNameEditor(): FileNameEditor? = when (this) {
    is ProjectItemExpander<*> -> editor.now?.getItemNameEditor()
    is FileEditor<*>          -> fileName
    is DirectoryEditor<*>     -> directoryName
    else                      -> error("Invalid project item editor $this")
}
