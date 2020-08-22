package hextant.blocky.editor

import hextant.blocky.Statement
import hextant.core.editor.ExpanderConfigurator

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<Statement>>({
    registerConstant("assign", ::AssignEditor)
    registerConstant("swap", ::SwapEditor)
    registerConstant("print", ::PrintEditor)
})