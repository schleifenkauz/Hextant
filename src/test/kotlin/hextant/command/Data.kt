package hextant.command

import hextant.core.expr.edited.IntLiteral

internal object Data {
    class Receiver(val isApplicable: Boolean) {
        override fun toString(): String = "Target: $isApplicable"
    }

    val commands = setOf<Command<Receiver, Unit>>(
        command {
            name = "1"
            description = "1"
            shortName = "1"
            executing { _, _ -> println("1") }
            applicableIf { it.isApplicable }
        },
        command {
            name = "print argument"
            description = "prints the argument"
            shortName = "printarg"
            addParameter {
                ofType<IntLiteral>()
                name = "arg"
                description = "The value to print"
            }
            executing { _, (p1) ->
                p1 as IntLiteral
                println(p1.value)
            }
            applicableIf { it.isApplicable }
        },
        command {
            name = "Print sum"
            description = "Prints the sum of two integers"
            shortName = "pntsm"
            addParameter {
                ofType<IntLiteral>()
                name = "op1"
                description = "The first operand"
            }
            addParameter {
                ofType<IntLiteral>()
                name = "op2"
                description = "The second operand"
            }
            executing { _, (p1, p2) ->
                p1 as IntLiteral
                p2 as IntLiteral
                println(p1.value + p2.value)
            }
            applicableIf { it.isApplicable }
        }
    )
}