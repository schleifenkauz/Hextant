package hextant.sample

import hextant.plugin.*
import hextant.sample.editor.*
import hextant.sample.rt.Interpreter
import hextant.sample.rt.RuntimeContext
import reaktive.value.binding.impl.notNull
import reaktive.value.binding.map
import reaktive.value.binding.or
import reaktive.value.now
import validated.*
import validated.reaktive.mapValidated

object SamplePlugin : PluginInitializer({
    stylesheet("sample.css")
    registerCommand<ProgramEditor, Unit> {
        name = "Execute Program"
        shortName = "execute"
        description = "Executes the program"
        defaultShortcut("Ctrl?+X")
        applicableIf { e -> e.result.now.isValid }
        executing { program, _ ->
            val result = program.result.now.force()
            val interpreter = Interpreter(result.functions)
            val ctx = RuntimeContext.root()
            interpreter.execute(result.main, ctx)
        }
    }
    registerInspection<ReferenceEditor> {
        id = "unresolved.variable"
        description = "Detects unresolved variable references"
        isSevere(true)
        message { "Variable ${inspected.result.now.force()} cannot be resolved" }
        checkingThat {
            val name = inspected.result.mapValidated { it.name }
            val type = inspected.context[Scope].resolve(name, inspected.line)
            type.notNull()
        }
    }
    registerInspection<FunctionCallEditor> {
        id = "unresolved.function"
        description = "Detects unresolved function calls"
        isSevere(true)
        message { "Function ${inspected.name.result.now.force()} cannot be resolved" }
        checkingThat {
            val name = inspected.name.result
            val def = inspected.context[GlobalScope].getDefinition(name)
            name.map { it.isInvalid } or def.notNull()
        }
    }
})