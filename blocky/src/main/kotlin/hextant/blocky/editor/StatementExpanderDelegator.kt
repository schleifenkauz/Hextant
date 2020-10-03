package hextant.blocky.editor

import hextant.blocky.Statement
import hextant.core.editor.ExpanderConfigurator

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<Statement>>({
    registerKey("assign", ::AssignEditor)
    registerKey("swap", ::SwapEditor)
    registerKey("print", ::PrintEditor)
})