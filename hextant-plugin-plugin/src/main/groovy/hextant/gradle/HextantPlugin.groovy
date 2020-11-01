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
                testImplementation hextantDependency(target, "test")
                kapt hextantDependency(target, "codegen")
            }
            kapt {
                annotationProcessors("hextant.codegen.MainProcessor")
                useBuildCache = true
                correctErrorTypes = target.hasProperty('correctErrorTypes') ? target.property('correctErrorTypes').toBoolean() : true
            }
            afterEvaluate {
                task('hextantPublish', type: Copy) {
                    from jar.outputs.files
                    into new File(System.getProperty('user.home'), 'hextant/plugins')
                    rename '(.+)', (config.pluginId ?: project.name) + ".jar"
                }
                build.finalizedBy(hextantPublish)
            }
            jar {
                from {
                    configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
                }
            }
        }
    }
}
