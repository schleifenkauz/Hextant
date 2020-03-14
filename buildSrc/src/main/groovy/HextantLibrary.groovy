import org.gradle.api.Plugin
import org.gradle.api.Project

class HextantLibrary implements Plugin<Project> {
    @Override
    void apply(final Project target) {
        target.version = '0.1-SNAPSHOT'
        def extension = target.extensions.create('hextant_lib', HextantLibraryExtension)
        target.apply plugin: 'org.jetbrains.dokka'
        target.apply plugin: 'com.bmuschko.nexus'

        target.archivesBaseName = target.name

        target.modifyPom {
            project {
                name target.name
                description extension.description
                url 'https://github.com/nkb03/hextant'
                inceptionYear '2020'

                scm {
                    url 'https://github.com/nkb03/hextant'
                    connection 'git:https://github.com/nkb03/hextant.git'
                    developerConnection 'git:git://github.com/nkb03/hextant.git'
                }

                licenses {
                    license {
                        name 'The Apache Software License, Version 2.0'
                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                        distribution 'repo'
                    }
                }

                developers {
                    developer {
                        id 'santaclaus'
                        name 'Nikolaus Knop'
                        email 'niko.knop003@gmail.com'
                    }
                }
            }
        }
        target.extraArchive {
            sources = true
            tests = true
            javadoc = true
        }
    }
}
