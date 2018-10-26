/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import java.nio.file.Files
import java.nio.file.Paths

internal object ResourcesSpec: Spek({
    val resourcesRoot = "D:\\Bibliotheken\\Mini Projekte\\Hextant\\hextant-core\\out\\production\\resources"
    describe("resources") {
        test("the root should be the resources dir in the build dir") {
            Resources.root.toString() shouldMatch equalTo(resourcesRoot)
        }
        test("all found resources should exist") {
            Resources.all().forEach { Files.exists(it) shouldMatch equalTo(true) }
        }
    }
})