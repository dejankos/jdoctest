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

    internal fun extract(): List<DocTest> {
        return buildModel().getElements(classFilter).flatMap { ctClass ->
            val classLevelComments = ctClass.comments.toMutableList()
            ctClass.methods.fold(classLevelComments) { acc, ctMethod ->
                acc.addAll(ctMethod.comments)
                acc
            }
                .flatMap {
                    extractDocTest(it)
                }
        }
    }

    private fun extractDocTest(comment: CtComment): List<DocTest> {
        if (comment.isNotJavadoc()) {
            return emptyList()
        }
        if (!comment.isNotDocTest()) {
            return emptyList()
        }

        return parseJavadoc(comment)
    }

    private fun buildModel(): CtModel {
        val spoon = Launcher()
        spoon.addInputResource(path)
        return spoon.buildModel()
    }

    private fun CtComment.isNotDocTest() = this.content.contains("jdoctest")

    private fun CtComment.isNotJavadoc() = this !is CtJavaDoc

    private fun parseJavadoc(comment: CtComment): List<DocTest> {
        var state = DocTestState.NONE
        val res = mutableListOf<DocTest>()
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
        if (state != DocTestState.CLOSED) {
            res.clear()
            res += ParseError("JDocTest parse error; javadoc fragment ${comment.docComment}")
        }

        return res
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
                Code(imports, code)
            }

    private enum class DocTestState {
        NONE, OPEN, CLOSED
    }

    sealed class DocTest
    data class Code(
        val docTestImports: List<String>,
        val docTestCode: List<String>
    ) : DocTest()

    data class ParseError(
        val error: String
    ) : DocTest()
}
