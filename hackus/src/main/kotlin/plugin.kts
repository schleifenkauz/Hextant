import hackus.editor.*
import hackus.view.*
import hextant.plugin.dsl.plugin

plugin {
    editor(::AlternativeEditor)
    editor(::CompoundEditor)
    editor(::DefinitionEditor)
    editor(::FQNameEditor)
    editor(::JIdentEditor)
    editor(::RightSideExpander)
    editor(::SubNodeEditor)
    editor(::TerminalEditor)
    editor(::FQNameListEditor)
    editor(::SubNodeListEditor)
    editor(::DefinitionListEditor)

    view(::AlternativeEditorControl)
    view(::FXCompoundEditorView)
    view(::DefinitionEditorControl)
    view(::FQNameEditorControl)
    view(::JIdentEditorControl)
    view(::SubNodeEditorControl)
    view(::TerminalEditorControl)
}