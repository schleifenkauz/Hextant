package hextant.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class HextantLibrary implements Plugin<Project> {
    @Override
    void apply(final Project target) {
        target.with {
            def extension = target.extensions.create('hextant_lib', HextantLibraryExtension)
            apply plugin: 'org.jetbrains.dokka'
            apply plugin: 'com.bmuschko.nexus'
            apply plugin: 'maven-publish'

            archivesBaseName = target.name

            modifyPom {
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
            extraArchive {
                sources = true
                tests = true
                javadoc = true
            }

            publishing {
                publications {
                    maven(MavenPublication) {
                        from components.java
                        artifact kotlinSourcesJar
                    }
                }
            }
            nexus {
                sign = true
                repositoryUrl = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
                snapshotRepositoryUrl = 'https://oss.sonatype.org/content/repositories/snapshots/'
            }
        }
    }
}
