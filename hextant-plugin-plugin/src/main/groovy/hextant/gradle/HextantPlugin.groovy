package hextant.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class HextantPlugin implements Plugin<Project> {
    static def hextantDependency(Project project, String lib) {
        if (project.parent != null && project.parent.name == "hextant") return project.project(":hextant-$lib")
        else "com.github.nkb03:hextant-$lib:1.0-SNAPSHOT"
    }

    @Override
    void apply(final Project target) {
        target.with {
            def config = extensions.create('hextant', HextantPluginExtension)
            apply plugin: 'kotlin-kapt'
            repositories {
                mavenCentral()
                jcenter()
                maven {
                    url 'https://oss.sonatype.org/content/repositories/snapshots'
                }
            }
            dependencies {
                compileOnly hextantDependency(target, "core")
                compileOnly hextantDependency(target, 'codegen')
                kapt hextantDependency(target, "codegen")
                testImplementation hextantDependency(target, "test")
            }
            kapt {
                annotationProcessors("hextant.codegen.MainProcessor")
                useBuildCache = true
                correctErrorTypes = target.hasProperty('correctErrorTypes') ? target.property('correctErrorTypes').toBoolean() : true
            }
            afterEvaluate {
                task('hextantPublish', type: Copy) {
                    from jar.outputs.files
                    def home = System.getenv('HEXTANT_HOME') ?: System.getProperty('user.home') + '/hextant'
                    into new File(home, 'plugins')
                    String name = project.name
                    if (name.startsWith("hextant-")) name = name.drop(7)
                    if (config.pluginId != null) name = config.pluginId
                    rename '(.+)', name + ".jar"
                    dependsOn(jar)
                }
            }
            jar {
                from {
                    configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
                }
            }
        }
    }
}
