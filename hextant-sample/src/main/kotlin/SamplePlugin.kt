import hextant.plugin.*
import hextant.simple.editor.ProgramEditor
import hextant.simple.rt.Interpreter
import hextant.simple.rt.RuntimeContext
import reaktive.value.now
import validated.force
import validated.isValid

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
})