import org.gradle.api.Plugin
import org.gradle.api.Project

class HextantPlugin implements Plugin<Project> {
    @Override
    void apply(final Project target) {
        def config = target.extensions.create('hextant_plugin', HextantPluginExtension)
        target.dependencies {
            compileOnly 'com.github.nkb03:hextant-core:0.1'
        }
        target.jar {
            manifest {
                attributes(
                        'Plugin-Name': config.name,
                        'Plugin-Author': config.author,
                        'Plugin-Description-Url': config.descriptionUrl,
                        'Plugin-Initializer': config.pluginInitializer,
                )
            }

            from {
                configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
            }
        }
    }
}
