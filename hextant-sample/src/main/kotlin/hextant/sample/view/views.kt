/**
 * @author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.ControlFactory
import hextant.core.view.*
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.core.view.ListEditorControl.Orientation.Vertical
import hextant.fx.view
import hextant.sample.editor.*
import javafx.scene.control.*
import reaktive.value.ReactiveString
import reaktive.value.ReactiveValue
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue
import validated.reaktive.mapValidated

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: IdentifierEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "identifier")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ReferenceEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "reference").apply {
        val id = editor.result.mapValidated { it.name }
        val type = editor.context[Scope].resolve(id, editor.line)
        attachInfoTooltip(type.orUnresolved())
    }

private fun ReactiveValue<Any?>.orUnresolved() = map { it?.toString() ?: "<unresolved>" }

private fun EditorControl<*>.attachInfoTooltip(info: ReactiveString) {
    val r = root as? Control ?: error("Cannot attach tooltip to $this")
    r.tooltip = Tooltip()
    r.tooltip.textProperty().bind(info.asObservableValue())
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: IntLiteralEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "int-literal")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: BooleanLiteralEditor, arguments: Bundle) = TokenEditorControl(
    editor, arguments,
    ConfiguredCompleter.withStringPool(CompletionStrategy.simple, listOf("true", "false")),
    styleClass = "keyword"
)

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: BinaryOperatorEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, BinaryOperatorCompleter, styleClass = "operator")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: BinaryExprEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.lhs)
        view(editor.operator)
        view(editor.rhs)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: FunctionCallEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.name).apply {
            val decl = editor.context[GlobalScope].getDefinition(editor.name.result)
            attachInfoTooltip(decl.orUnresolved())
        }
        operator("(")
        view(editor.arguments) {
            set(ListEditorControl.ORIENTATION, Horizontal)
            set(ListEditorControl.CELL_FACTORY) { ListEditorControl.SeparatorCell(", ") }
        }
        operator(")")
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ExprExpander, arguments: Bundle) = ExpanderControl(editor, arguments, ExprCompleter)

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: StatementExpander, arguments: Bundle) = ExpanderControl(
    editor, arguments,
    editor.config.completer(CompletionStrategy.simple)
)

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: PrintStatementEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("print")
        space()
        view(editor.expr)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ExprStatementEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.expr)
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: DefinitionEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.type)
        space()
        view(editor.name)
        operator("=")
        view(editor.value)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: AssignmentEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.name)
        operator("=")
        view(editor.value)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: AugmentedAssignmentEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.name)
        operator("=")
        view(editor.operator)
        view(editor.name)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: BlockEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.statements) {
        set(ListEditorControl.ORIENTATION, Vertical)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ControlFlowStatementEditor, arguments: Bundle) = TokenEditorControl(
    editor, arguments,
    ConfiguredCompleter.withStringPool(CompletionStrategy.simple, listOf("break", "continue")),
    styleClass = "keyword"
)

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ReturnStatementEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("return")
        space()
        view(editor.expr)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ForLoopEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("for")
        operator("(")
        view(editor.initializer)
        operator(";")
        view(editor.condition)
        operator(";")
        view(editor.after)
        operator(")")
    }
    indented { view(editor.body) }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: IfStatementEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("if")
        operator("(")
        view(editor.condition)
        operator(")")
    }
    indented { view(editor.consequence) }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: WhileLoopEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        keyword("while")
        operator("(")
        view(editor.condition)
        operator(")")
    }
    indented { view(editor.body) }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: SimpleTypeEditor, arguments: Bundle) = TokenEditorControl(
    editor, arguments,
    ConfiguredCompleter.withStringPool(CompletionStrategy.simple, listOf("int", "bool", "void")),
    styleClass = "type"
)

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ParameterEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.type)
        space()
        view(editor.name)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: FunctionDefinitionEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.returnType)
        space()
        view(editor.name)
        operator("(")
        view(editor.parameters) {
            set(ListEditorControl.CELL_FACTORY) { ListEditorControl.SeparatorCell(",") }
            set(ListEditorControl.ORIENTATION, Horizontal)
        }
        operator(")")
    }
    indented { view(editor.body) }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: ProgramEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    view(editor.functions) {
        set(ListEditorControl.ORIENTATION, Vertical)
        set(ListEditorControl.EMPTY_DISPLAY) { Button("Add function") }
    }
    keyword("main:")
    indented { view(editor.main) }
}