package hextant.test

import hextant.main.HextantApp
import hextant.project.ProjectType
import java.io.File
import kotlin.reflect.jvm.jvmName

fun showTestProject(type: ProjectType) {
    val clazz = type::class.jvmName
    File(System.getProperty("user.home"), "hextant/testProjects/$clazz").delete()
    HextantApp.main("--create=$clazz", "--save=false", "--plugin-source=classpath", "~/hextant/testProjects/$clazz")
}