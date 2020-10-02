/**
 *@author Nikolaus Knop
 */

package hextant.lispy

@Suppress("EnumEntryName")
enum class Predefined(val definition: SExpr) {
    map(
        call(
            "lambda".s,
            call("f".s, "lst".s),
            call(
                "if".s,
                call("nil?".s, "lst".s),
                Nil,
                "else".s,
                call(
                    "cons".s,
                    call("f".s, call("car".s, "lst".s)),
                    call("map".s, "f".s, call("cdr".s, "lst".s))
                )
            )
        )
    ),
    dbg(
        call(
            "macro".s,
            call("e".s),
            call(
                "list".s,
                quote("begin".s),
                call("list".s, quote("print".s), call("quote".s, "e".s)),
                call("list".s, quote("print".s), "e".s)
            )
        )
    );

    companion object : Map<String, SExpr> by (values().associate { it.name to it.definition })
}