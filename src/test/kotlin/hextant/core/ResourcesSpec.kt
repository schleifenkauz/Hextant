/**
 *@author Nikolaus Knop
 */

package hextant.core

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.core.impl.Resources
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.xdescribe
import java.nio.file.Files

internal object ResourcesSpec: Spek({
    val resourcesRoot = "D:\\Bibliotheken\\Mini Projekte\\Hextant\\hextant-core\\out\\production\\resources"
    xdescribe("resources") {
        test("the root should be the resources dir in the build dir") {
            Resources.root.toString() shouldMatch equalTo(resourcesRoot)
        }
        test("all found resources should exist") {
            Resources.all.forEach { Files.exists(it) shouldMatch equalTo(true) }
        }
    }
})