package hextant.project.editor

import hextant.core.editor.ResultStrategy

internal object FileNameResultStrategy : ResultStrategy<String> {
    override fun default(): String = "<invalid>"

    override fun unwrap(result: String): Any? = result.takeIf { isValid(it) }

    override fun isValid(result: String): Boolean = result != "<invalid>"
}