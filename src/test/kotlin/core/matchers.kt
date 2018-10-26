/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import com.natpryce.hamkrest.Matcher
import kotlin.reflect.KClass

fun <T : Any> instanceOf(cls: KClass<out T>): Matcher<Any> =
        Matcher("A value that is instance of $cls") { cls.isInstance(it) }

inline fun <reified T: Any> instanceOf() = instanceOf(T::class)