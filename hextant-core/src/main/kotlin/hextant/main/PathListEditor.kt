/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.bundle.Internal
import hextant.core.editor.ListEditor
import hextant.force
import hextant.serial.SerialProperties
import kserial.createOutput
import reaktive.value.now
import java.nio.file.Files
import java.nio.file.Path

class PathListEditor(context: Context) :
    ListEditor<Path, PathEditor>(context) {
    override fun createEditor(): PathEditor? {
        val pc = context[Internal, PathChooser]
        val initial = pc.choosePath(context) ?: return null
        return PathEditor(context, initial)
    }

    override fun editorAdded(editor: PathEditor, index: Int) {
        val path = editor.result.now.force()
        val root = path.resolve("project.hxt")
        if (Files.exists(root)) return
        val projectType = context[ProjectType]
        val newProject = projectType.createProjectRoot(context)
        val serial = context[SerialProperties.serial]
        val ctx = context[SerialProperties.serialContext]
        val output = serial.createOutput(root, ctx)
        output.writeObject(newProject)
        output.close()
    }
}