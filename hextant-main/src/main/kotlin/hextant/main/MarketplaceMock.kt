/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.plugins.*
import hextant.plugins.Plugin.Type
import hextant.plugins.ProjectType
import java.io.File

object MarketplaceMock : Marketplace {
    override fun getPlugins(searchText: String, limit: Int, types: Set<Type>, excluded: Set<String>): List<Plugin> {
        TODO("not implemented")
    }

    override fun getImplementation(aspect: String, feature: String): ImplementationCoord? {
        TODO("not implemented")
    }

    override fun availableProjectTypes(): List<LocatedProjectType> =
        listOf(
            LocatedProjectType(ProjectType("Haskell Project", "haskell.HaskellProject"), "haskell"),
            LocatedProjectType(ProjectType("Kotlin Project", "kotlin.KotlinProject"), "kotlin")
        )

    override fun getJarFile(id: String): File? {
        TODO("not implemented")
    }

    override fun upload(jar: File) {
        TODO("not implemented")
    }
}