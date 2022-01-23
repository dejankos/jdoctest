package io.github.dejankos.jdoctest.core

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.io.path.deleteExisting
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeBytes

class JDocCompiler(
    private val docsTest: List<DocTestContext>,
    private val classpathElements: List<String>
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val jDocTestPath by lazy {
        Path.of(System.getProperty(TMP_DIR_PROPERTY), "jdoctest_compile")
    }

    private companion object {
        const val CP_PROPERTY = "java.class.path"
        const val TMP_DIR_PROPERTY = "java.io.tmpdir"
    }

    fun runAll() {
        adjustClasspath()
        scopedDir(jDocTestPath) { _ ->
            docsTest.forEach { ctx ->
                runClassDocTest(ctx)
            }
        }
    }

    private fun runClassDocTest(
        docTestContext: DocTestContext
    ) {
        docTestContext.docsCode.forEach { docTestCode ->
            scopedDir(Path.of(jDocTestPath.toString(), "${System.currentTimeMillis()}")) { classDir ->
                println("cp" + System.getProperty(CP_PROPERTY))
                try {
                    compileJDocTest(classDir, docTestContext.typeInfo, docTestCode)
                    createClassInstance(classDir, docTestContext.typeInfo.fullJDocTestClassName()).run()
                } catch (t: Throwable) {
                    throw ExecutionException(t.message ?: "UNKNOWN", t)
                }
            }
        }
    }

    private fun compileJDocTest(
        workingDir: Path,
        typeInfo: TypeInfo,
        docTestCode: DocTestCode
    ) {
        val source = createClassSource(workingDir, typeInfo, docTestCode)
        compileClassSource(source).diagnostics.forEach {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it.kind) {
                Diagnostic.Kind.ERROR -> throw CompileException(
                    it.getMessage(),
                    it.lineNumber.toInt(),
                    it.source.toString()
                )
                Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING -> log.warn(it.getMessage())
                Diagnostic.Kind.NOTE -> log.info(it.getMessage())
            }
        }
    }

    private fun adjustClasspath() {
        System.setProperty(
            CP_PROPERTY,
            System.getProperty(CP_PROPERTY) + (
                classpathElements
                    .joinToString(separator = "", prefix = File.pathSeparator) { it }
                )
        )
    }

    private fun createClassInstance(path: Path, fullClassName: String): Runnable {
        val classLoader = URLClassLoader.newInstance(
            arrayOf(
                path.toUri().toURL(),
                Path.of("/home/dkos/IdeaProjects/jdoctest/example/target/classes").toUri().toURL()
            )
        )
        return classLoader.loadClass(fullClassName)
            .getDeclaredConstructor()
            .newInstance()
            as Runnable
    }

    private fun createClassSource(
        path: Path,
        typeInfo: TypeInfo,
        docTestCode: DocTestCode
    ): Path {
        val pkgDir = Files.createDirectories(
            Path.of(
                path.toString(),
                *typeInfo.`package`.split(".").toTypedArray(),
            )
        )
        val source = Files.createFile(
            Path.of(pkgDir.toString(), "${typeInfo.jDocTestClassName()}.java")
        )

        source.writeBytes(bindDocTestCode(typeInfo, docTestCode).toByteArray())
        return source
    }

    private fun compileClassSource(source: Path): DiagnosticCollector<JavaFileObject> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

        val optionList: MutableList<String> = ArrayList()
        optionList.add("-classpath")
        optionList.add("/home/dkos/IdeaProjects/jdoctest/example/target/classes")

        fileManager.use {
            val fileObject = fileManager.getJavaFileObjects(source)
            compiler.getTask(null, fileManager, diagnostics, optionList, null, fileObject).call()
        }

        return diagnostics
    }

    private fun bindDocTestCode(typeInfo: TypeInfo, docTestCode: DocTestCode) =
        """
            package ${typeInfo.`package`};
            ${typeInfo.imports.joinAsImportMultiline()}
            ${docTestCode.docTestImports.joinMultiline()}
            
            public class ${typeInfo.name}_JDocTest implements Runnable {
                public void run() {
                    ${docTestCode.docTestCode.joinMultiline()}
                }
            }
        """

    private fun scopedDir(path: Path, f: (Path) -> Unit) {
        val dir = Files.createDirectories(path)
        try {
            f(dir)
        } finally {
            deleteDir(path)
        }
    }

    private fun deleteDir(path: Path) {
        if (path.isDirectory()) {
            path.listDirectoryEntries().forEach {
                deleteDir(it)
            }
        }

        path.deleteExisting()
    }

    private fun TypeInfo.fullJDocTestClassName() = "${this.`package`}.${this.jDocTestClassName()}"
    private fun TypeInfo.jDocTestClassName() = "${this.name}_JDocTest"
    private fun List<String>.joinMultiline() = this.joinToString(separator = "\n") { it }
    private fun List<String>.joinAsImportMultiline() = this.joinToString(separator = "\n") { "import $it;" }
    private fun <S> Diagnostic<S>.getMessage() = this.getMessage(Locale.getDefault())
}
