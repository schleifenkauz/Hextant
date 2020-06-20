import bundles.SimpleReactiveProperty
import hextant.command.Command
import hextant.command.executingCompoundEdit
import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.context.Context
import hextant.context.HextantPlatform.propertyChangeHandlers
import hextant.core.view.*
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.expr.*
import hextant.expr.Operator.Plus
import hextant.expr.editor.*
import hextant.inspect.Severity.Warning
import hextant.plugin.dsl.PluginInitializer
import hextant.undo.compoundEdit
import org.controlsfx.glyphfont.FontAwesome
import reaktive.value.binding.and
import reaktive.value.binding.map
import reaktive.value.now
import validated.*

object ExprPlugin : PluginInitializer({
    author = "Nikolaus Knop"
    name = "Hextant Expressions"
    defaultEditor(::IntLiteralEditor)
    defaultEditor(::OperatorEditor)
    defaultEditor(::OperatorApplicationEditor)
    defaultEditor(::SumEditor)
    defaultEditor(::ExprExpander)
    defaultEditor(::ExprListEditor)
    compoundView { e: OperatorApplicationEditor ->
        line {
            operator("(")
            view(e.operand1)
            view(e.operator)
            view(e.operand2)
            operator(")")
        }
    }
    view { editor: ExprExpander, args ->
        val c = CompoundCompleter<Context, Any>()
        c.addCompleter(ExprExpander.config.completer(CompletionStrategy.simple))
        c.addCompleter(SpecialNumbers)
        ExpanderControl(editor, args, c)
    }
    tokenEditorView<OperatorEditor>("operator")
    tokenEditorView<IntLiteralEditor>(styleClass = "decimal-editor", completer = SpecialNumbers)
    compoundView { e: SumEditor ->
        line {
            keyword("sum")
            space()
            view(e.expressions)
        }
    }
    view<ExprListEditor> { editor, args ->
        ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, args, Horizontal).apply {
            cellFactory = { ListEditorControl.SeparatorCell(", ") }
        }
    }
    registerConversion<Expr, Int> { expr -> valid(expr.value) }
    registerCommand<ExprEditor<*>, Int> {
        name = "Evaluate Expression"
        shortName = "eval"
        applicableIf { exprEditor -> exprEditor.result.now.isValid }
        description = "Evaluates the selected expression and prints it to the console"
        executing { editor, _ ->
            val e = editor.result.now.force()
            val v = e.value
            v
        }
    }
    registerCommand<OperatorEditor, Unit> {
        name = "Flip operands"
        shortName = "flip_op"
        description = "Flips the both operands in this operator application"
        type = Command.Type.SingleReceiver
        applicableIf { oe ->
            val oae = oe.parent as? OperatorApplicationEditor ?: return@applicableIf false
            oae.operator.result.now.map { it.isCommutative }.ifInvalid { false }
        }
        executingCompoundEdit { oe, _ ->
            val oae = oe.parent as OperatorApplicationEditor
            val expander1 = oae.operand1
            val editableOp1 = expander1.editor.now
            val expander2 = oae.operand2
            val editableOp2 = expander2.editor.now
            if (editableOp2 != null) expander1.setEditor(editableOp2)
            if (editableOp1 != null) expander2.setEditor(editableOp1)
        }
    }
    registerCommand<OperatorApplicationEditor, Unit> {
        name = "Collapse expression"
        shortName = "collapse"
        description = "Partially evaluate the selected expression"
        applicableIf { oae ->
            oae.result.now.isValid && oae.expander != null
        }
        executingCompoundEdit { oae, _ ->
            val ex = oae.expander as ExprExpander
            val res = oae.result.now.force().value
            val editable = IntLiteralEditor(context, res.toString())
            ex.setEditor(editable)
        }
    }
    registerCommand<ExprEditor<*>, Unit> {
        name = "Unwrap expression"
        shortName = "unwrap"
        description = "Unwrap an expression by replacing its outer application with itself"
        applicableIf {
            it.parent is OperatorApplicationEditor && it.parent!!.expander is ExprExpander
        }
        executingCompoundEdit { editor, _ ->
            val parentExpander = editor.parent!!.expander as ExprExpander
            parentExpander.setEditor(editor)
        }
    }
    registerInspection<OperatorApplicationEditor> {
        id = "identical"
        description = "Prevent identical operations"
        isSevere(true)
        preventingThat {
            val operandIsZero = inspected.operand2.result.map { it.orNull() is IntLiteral && it.force().value == 0 }
            val operatorIsPlus = inspected.operator.result.map { it.orNull() == Plus }
            operatorIsPlus.and(operandIsZero)
        }
        message { "Operation doesn't change the result" }
        addFix {
            description = "Shorten expression"
            applicableIf {
                inspected.expander is ExprExpander
            }
            fixingBy {
                val expander = inspected.expander as ExprExpander
                expander.setEditor(inspected.operand1)
            }
        }
    }
    registerInspection<IntLiteralEditor> {
        id = "zero"
        description = "Prevent '0' Literals"
        message { "Literal is '0'" }
        severity(Warning)
        preventingThat { inspected.result.map { it.orNull()?.value == 0 } }
        addFix {
            description = "Set to '1'"
            fixingBy {
                inspected.setText("1")
            }
        }
        addFix {
            description = "Set to '2'"
            fixingBy {
                inspected.setText("2")
            }
        }
    }
    registerCommand<ExprExpander, Unit> {
        description =
            "Wraps the current expression in an operator expression with the current expression being the left operand"
        name = "Wrap in operator expression"
        shortName = "wrap_op"
        addParameter {
            ofType<Operator>()
            description = "The operator being applied"
            name = "operator"
        }
        applicableIf { it.isExpanded }
        executing { expander, (operator) ->
            expander.context.compoundEdit("Wrap with $operator") {
                val editor = expander.editor.now!!
                operator as Operator
                val app = OperatorApplicationEditor(editor.context)
                expander.setEditor(app)
                app.operator.setText(operator.toString())
                app.operand1.setEditor(editor)
            }
        }
    }
    val color = SimpleReactiveProperty<String?>("color")
    context[propertyChangeHandlers].forContext<AbstractTokenEditorControl>().handle(color) { control, c ->
        control.root.style = c?.let { "-fx-text-fill: $c" }
    }
    registerCommand<TokenEditorControl, Unit> {
        description = "Sets the text fill"
        name = "Set Color"
        shortName = "color"
        defaultShortcut("Ctrl+G")
        addParameter {
            name = "color"
            ofType<String>()
            description = "The text fill"
        }
        executing { v, (c) -> v.arguments[color] = c as String? }
    }
    registerCommand<Any, Unit> {
        description = "Throws an exception"
        name = "Error"
        shortName = "error"
        executing { _, _ -> throw AssertionError("error") }
    }
    stylesheet("expr.css")
})