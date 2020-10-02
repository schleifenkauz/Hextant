import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class HextantPlugin implements Plugin<Project> {
    @Override
    void apply(final Project target) {
        target.with {
            def config = extensions.create('hextant', HextantPluginExtension)
            apply plugin: 'kotlin-kapt'
            target.dependencies {
                if (parent != null && parent.name == "hextant") compileOnly project(":hextant-core")
                else compileOnly 'com.github.nkb03:hextant-core:0.1-SNAPSHOT'
                testImplementation project(":hextant-test")
                kapt 'com.github.nkb03:hextant-codegen:0.1-SNAPSHOT'
            }
            kapt {
                annotationProcessors("hextant.codegen.MainProcessor")
                useBuildCache = true
                correctErrorTypes = true
            }
            task('hextantPublish', type: Copy) {
                from jar.outputs.files
                into 'C:/Users/Nikolaus Knop/hextant/plugins'
                rename '(.+)', config.pluginName ?: project.name
            }
            build.finalizedBy(hextantPublish)
            jar {
                from {
                    configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
                }
            }
        }
    }
}
