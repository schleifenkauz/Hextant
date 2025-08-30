package hextant.core.view

import fxutils.prompt.SelectorPrompt
import hextant.core.editor.ChoiceSource
import reaktive.value.now

class ChoiceEditorSelectorPrompt<C : Any>(
    private val editor: ChoiceSource<C>
) : SelectorPrompt<C>("Choose option") {
    override fun extractText(option: C): String = editor.toString(option).now

    override fun options(): List<C> = editor.choices()
}