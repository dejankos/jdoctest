package io.github.dejankos.jdoctest.core

import spoon.Launcher
import spoon.javadoc.internal.JavadocInlineTag
import spoon.javadoc.internal.JavadocSnippet
import spoon.reflect.CtModel
import spoon.reflect.code.CtComment
import spoon.reflect.code.CtJavaDoc
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter

class DocTestParser(private val path: String) {

    companion object {
        private val classFilter = TypeFilter(CtClass::class.java)
    }

    internal fun extract(): List<DocTestClassData> {
        return buildModel().getElements(classFilter).flatMap { ctClass ->
            val allClassComments = ctClass.comments.toMutableList()
            ctClass.methods.forEach { allClassComments.addAll(it.comments) }

            allClassComments
                .map { extractDocTest(it) }
                .filter { it.isNotEmpty() }
                .map { DocTestClassData(ctClass.getClassContext(), it) }
        }
    }

    private fun extractDocTest(comment: CtComment) = when {
        comment.isNotJavadoc() -> emptyList()
        comment.isNotDocTest() -> emptyList()
        else -> parseJavadoc(comment)
    }

    private fun buildModel(): CtModel {
        val spoon = Launcher()
        spoon.addInputResource(path)
        return spoon.buildModel()
    }

    private fun parseJavadoc(comment: CtComment): List<DocTestCode> {
        var state = DocTestState.NONE
        val res = mutableListOf<DocTestCode>()
        for (e in comment.asJavaDoc().javadocElements) {
            when (e) {
                is JavadocSnippet -> {
                    if (e.toText().contains("<jdoctest>")) {
                        state = DocTestState.OPEN
                        continue
                    }
                    if (e.toText().contains("</jdoctest>")) {
                        state = DocTestState.CLOSED
                        continue
                    }
                }
                is JavadocInlineTag -> {
                    if (state == DocTestState.OPEN && e.type == JavadocInlineTag.Type.CODE) {
                        res += extractDocCode(e)
                    }
                }
            }
        }

        return when (state) {
            DocTestState.CLOSED -> res
            else -> throw ParseException("JDocTest parse error; javadoc fragment ${comment.docComment}")
        }
    }

    private fun extractDocCode(javadoc: JavadocInlineTag) =
        javadoc.content.lineSequence()
            .fold(mutableListOf<String>() to mutableListOf<String>()) { acc, line ->
                when (line.contains("import")) {
                    true -> {
                        acc.first += line
                    }
                    else -> {
                        acc.second += line
                    }
                }
                acc
            }
            .run {
                val (imports, code) = this
                DocTestCode(imports, code)
            }

    private fun CtComment.isNotDocTest() = !this.content.contains("jdoctest")

    private fun CtComment.isNotJavadoc() = this !is CtJavaDoc

    private fun CtClass<*>.getClassContext() = ClassContext(
        this.`package`.qualifiedName,
        this.simpleName,
        this.getUsedTypes(false).map { it.toString() }
    )

    private enum class DocTestState {
        NONE, OPEN, CLOSED
    }

    data class DocTestClassData(
        val classCtx: ClassContext,
        val docsCode: List<DocTestCode>,
    )

    data class ClassContext(
        val classPackage: String,
        val className: String,
        val classImports: List<String>,
    )

    data class DocTestCode(
        val docTestImports: List<String>,
        val docTestCode: List<String>
    )
}
