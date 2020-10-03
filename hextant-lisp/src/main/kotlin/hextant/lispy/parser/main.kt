/**
 * @author Nikolaus Knop
 */

package hextant.lispy.parser

import hextant.lispy.SExpr
import hextant.lispy.rt.Env
import hextant.lispy.rt.evaluate
import java.io.*
import java.net.URL

fun evaluate(input: Reader, env: Env = Env.root()): SExpr = parseExpr(input).evaluate(env)

fun evaluate(str: String, env: Env = Env.root()): SExpr = evaluate(StringReader(str), env)

fun evaluate(url: URL, env: Env = Env.root()): SExpr = evaluate(url.openStream().bufferedReader(), env)

fun evaluate(file: File, env: Env = Env.root()): SExpr = evaluate(file.bufferedReader(), env)