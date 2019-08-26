package hextant.blocky.editor

import hextant.blocky.Statement
import hextant.codegen.ExpanderConfigurator

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<Statement>>({
    registerConstant("assign", ::AssignEditor)
    registerConstant("swap", ::SwapEditor)
})