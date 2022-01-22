package io.github.dejankos.jdoctest.core

import org.slf4j.LoggerFactory
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.tools.*
import kotlin.io.path.*

class JDocCompiler(
    private val docsTest: List<DocTestParser.DocTestClassData>
) {
    private val log = LoggerFactory.getLogger("JDocTestCompiler")

    fun compile() {
        val dir = createTempDirectory("jdoctest_compile")

        for (docTestClassData in docsTest) {
            for (docTestCode in docTestClassData.docsCode) {
                val classAsString = bindDocTestCode(docTestClassData.classCtx, docTestCode)

                val classDir = Files.createDirectory(Path.of(dir.toString(), "${System.currentTimeMillis()}"))

                val jdocDir = Files.createDirectories(
                    Path.of(classDir.toString(),  "io", "github", "dejankos")
                )

                val temp = Files.createFile(
                    Path.of(jdocDir.toString(), "${docTestClassData.classCtx.className}_JDocTest.java")
                )
                temp.writeBytes(classAsString.toByteArray())

                val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
                val diagnostics = DiagnosticCollector<JavaFileObject>()
                val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

                val fileObject = fileManager.getJavaFileObjects(temp)

                val call = compiler.getTask(null, fileManager, diagnostics, null, null, fileObject).call()

                val classLoader = URLClassLoader.newInstance(arrayOf(classDir.toUri().toURL()))

                val cls = classLoader.loadClass("io.github.dejankos.Example_JDocTest")

                val newInstance = cls.getDeclaredConstructor().newInstance() as Runnable
                newInstance.run()

                diagnostics.diagnostics.forEach {
                    when (it.kind) {
                        Diagnostic.Kind.ERROR -> throw CompileException(it.getMessage())
                        Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING -> log.warn(it.getMessage())
                        Diagnostic.Kind.NOTE -> log.info(it.getMessage())
                        else -> {
                        }
                    }
                }
            }
        }
//        dir.listDirectoryEntries().forEach { it.deleteExisting() }
//        dir.deleteExisting()
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

    private fun List<String>.joinMultiline() = this.joinToString(separator = "\n") { it }
    private fun List<String>.joinAsImportMultiline() = this.joinToString(separator = "\n") { "import $it;" }
    private fun <S> Diagnostic<S>.getMessage() = this.getMessage(Locale.getDefault())
}
