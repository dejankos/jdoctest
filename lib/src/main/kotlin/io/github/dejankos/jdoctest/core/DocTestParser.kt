package io.github.dejankos.jdoctest.core

import spoon.Launcher
import spoon.javadoc.internal.JavadocInlineTag
import spoon.javadoc.internal.JavadocSnippet
import spoon.reflect.CtModel
import spoon.reflect.code.CtComment
import spoon.reflect.code.CtJavaDoc
import spoon.reflect.declaration.CtPackage
import spoon.reflect.declaration.CtType
import spoon.reflect.visitor.filter.TypeFilter

internal class DocTestParser(private val path: String) {

    private companion object {
        private val javadocFilter = TypeFilter(CtJavaDoc::class.java)
    }

    internal fun extract(): List<DocTestContext> {
        return buildModel().getElements(javadocFilter)
            .filter { it.isJavadoc() }
            .filter { it.isDocTest() }
            .mapNotNull { jDoc ->
                extractTypeData(jDoc)?.let {
                    DocTestContext(it, parseJavadoc(jDoc))
                }
            }
    }

    private fun extractTypeData(comment: CtComment): TypeInfo? {
        var parent = comment.parent
        while (parent != null && parent !is CtPackage) {
            if (parent is CtType<*>) {
                return parent.getClassContext()
            }
            parent = parent.parent
        }

        return null
    }

    private fun buildModel(): CtModel {
        val spoon = Launcher()
        spoon.addInputResource(path)
        return spoon.buildModel()
    }

    private fun parseJavadoc(comment: CtJavaDoc): List<DocTestCode> {
        var state = DocTestState.NONE
        val res = mutableListOf<DocTestCode>()
        for (e in comment.javadocElements) {
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

    private fun CtComment.isDocTest() = this.content.contains("jdoctest")

    private fun CtComment.isJavadoc() = this is CtJavaDoc

    private fun CtType<*>.getClassContext() = TypeInfo(
        this.`package`.qualifiedName,
        this.simpleName,
        this.getUsedTypes(false).map { it.toString() }
    )

    private enum class DocTestState {
        NONE, OPEN, CLOSED
    }
}
