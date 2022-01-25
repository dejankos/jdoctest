package io.github.dejankos.plugin

import io.github.dejankos.jdoctest.core.JDocTest
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

@Mojo(name = "jdoctest", defaultPhase = LifecyclePhase.VERIFY)
class JDocTestMojo : AbstractMojo() {

    @Parameter(defaultValue = ".", readonly = true)
    lateinit var path: String

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    override fun execute() {
        try {
            JDocTest().processSources(path, project.runtimeClasspathElements)
        } catch (e: RuntimeException) {
            throw MojoExecutionException(e.message, e)
        }
    }
}
