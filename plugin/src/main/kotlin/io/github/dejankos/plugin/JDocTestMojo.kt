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

    @Parameter(defaultValue = ".")
    lateinit var docPath: String

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    override fun execute() {
        try {
            val cp = project.runtimeClasspathElements + projectDependencies()
            JDocTest().processSources(docPath, cp)
        } catch (e: RuntimeException) {
            throw MojoExecutionException(e.message, e.cause ?: e)
        }
    }

    private fun projectDependencies() = project.basedir?.listFiles()
        ?.filter { it.path.contains("target") }
        ?.flatMap { target ->
            target?.listFiles()
                ?.find { it.path.contains("dependency") }
                ?.listFiles()
                ?.map { it.absolutePath } ?: emptyList()
        } ?: throw IllegalStateException("Can't access project base dir")
}
