package io.github.dejankos.jdoctest.core

import org.slf4j.LoggerFactory
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.io.path.*

class JDocCompiler(
    private val docsTest: List<DocTestParser.DocTestClassData>
) {
    private val log = LoggerFactory.getLogger("JDocTestCompiler")
    private val jDocTestPath by lazy {
        Path.of(System.getProperty("java.io.tmpdir"), "jdoctest_compile")
    }

    fun runAll() {
        scopedDir(jDocTestPath) { _ ->
            docsTest.forEach { docTestClassData ->
                runClassDocTest(docTestClassData)
            }
        }
    }

    private fun runClassDocTest(
        docTestClassData: DocTestParser.DocTestClassData
    ) {
        docTestClassData.docsCode.forEach { docTestCode ->
            scopedDir(Path.of(jDocTestPath.toString(), "${System.currentTimeMillis()}")) { classDir ->
                compileJDocTest(classDir, docTestClassData.classCtx, docTestCode)
                createClassInstance(classDir, docTestClassData.classCtx.fullJDocTestClassName()).run()
            }
        }
    }

    private fun compileJDocTest(
        workingDir: Path,
        classCtx: DocTestParser.ClassContext,
        docTestCode: DocTestParser.DocTestCode
    ) {
        val source = createClassSource(workingDir, classCtx, docTestCode)
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

    private fun createClassInstance(path: Path, fullClassName: String): Runnable {
        val classLoader = URLClassLoader.newInstance(arrayOf(path.toUri().toURL()
        ,
            Path("/home/dkos/IdeaProjects/testing_jdoctest/target/classes").toUri().toURL()
        ))
        return classLoader.loadClass(fullClassName)
            .getDeclaredConstructor()
            .newInstance()
            as Runnable
    }

    private fun createClassSource(
        path: Path,
        classCtx: DocTestParser.ClassContext,
        docTestCode: DocTestParser.DocTestCode
    ): Path {
        val pkgDir = Files.createDirectories(
            Path.of(
                path.toString(),
                *classCtx.classPackage.split(".").toTypedArray(),
            )
        )
        val source = Files.createFile(
            Path.of(pkgDir.toString(), "${classCtx.jDocTestClassName()}.java")
        )

        source.writeBytes(bindDocTestCode(classCtx, docTestCode).toByteArray())
        return source
    }

    private fun compileClassSource(source: Path): DiagnosticCollector<JavaFileObject> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

        val optionList: MutableList<String> = ArrayList()
        optionList.add("-classpath")
        optionList.add("/home/dkos/IdeaProjects/testing_jdoctest/target/classes")

        fileManager.use {
            val fileObject = fileManager.getJavaFileObjects(source)
            compiler.getTask(null, fileManager, diagnostics, optionList, null, fileObject).call()
        }

        return diagnostics
    }

    private fun scopedDir(path: Path, f: (Path) -> Unit) {
        val dir = Files.createDirectories(path)
        try {
            f(dir)
        } finally {
            deleteDir(path)
        }
    }

    private fun bindDocTestCode(classContext: DocTestParser.ClassContext, docTestCode: DocTestParser.DocTestCode) =
        """
            package ${classContext.classPackage};
            ${classContext.classImports.joinAsImportMultiline()}
            ${docTestCode.docTestImports.joinMultiline()}
            
            public class ${classContext.className}_JDocTest implements Runnable {
                public void run() {
                    ${docTestCode.docTestCode.joinMultiline()}
                }
            }
        """

    private fun deleteDir(path: Path) {
        if (path.isDirectory()) {
            path.listDirectoryEntries().forEach {
                deleteDir(it)
            }
        }

        path.deleteExisting()
    }

    private fun DocTestParser.ClassContext.fullJDocTestClassName() = "${this.classPackage}.${this.jDocTestClassName()}"
    private fun DocTestParser.ClassContext.jDocTestClassName() = "${this.className}_JDocTest"
    private fun List<String>.joinMultiline() = this.joinToString(separator = "\n") { it }
    private fun List<String>.joinAsImportMultiline() = this.joinToString(separator = "\n") { "import $it;" }
    private fun <S> Diagnostic<S>.getMessage() = this.getMessage(Locale.getDefault())
}
