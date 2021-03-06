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

    private val typeInfos = typeMemo<CtType<*>, TypeInfo> { it.getTypeContext() }

    internal fun extract() =
        buildModel().getElements(javadocFilter)
            .filter { it.isJavadoc() }
            .filter { it.isDocTest() }
            .mapNotNull { jDoc ->
                extractTypeData(jDoc)?.let {
                    DocTestContext(it, parseJavadoc(jDoc))
                }
            }

    private fun extractTypeData(comment: CtComment): TypeInfo? {
        var parent = comment.parent
        while (parent != null && parent !is CtPackage) {
            if (parent is CtType<*>) {
                return typeInfos(parent)
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
            else -> throw ParseException(
                "JDocTest parse error; javadoc fragment ${comment.docComment}",
                comment.content
            )
        }
    }

    private fun extractDocCode(javadoc: JavadocInlineTag) =
        javadoc.content.lineSequence()
            .fold(listOf<String>() to listOf<String>()) { acc, line ->
                if (line.contains("import"))
                    acc.first + line to acc.second
                else
                    acc.first to acc.second + line
            }
            .run {
                val (imports, code) = this
                DocTestCode(imports, code, javadoc.content)
            }

    private fun CtComment.isDocTest() = this.content.contains("jdoctest")

    private fun CtComment.isJavadoc() = this is CtJavaDoc

    private fun CtType<*>.getTypeContext() = TypeInfo(
        this.`package`.qualifiedName,
        this.simpleName,
        this.getUsedTypes(false).map { it.toString().importTypeErase() }
    )

    private fun String.importTypeErase() =
        this.toCharArray()
            .takeWhile { it != '<' }
            .fold(StringBuilder()) { acc, next ->
                acc.append(next)
            }
            .also {
                it.append(";")
            }
            .toString()

    private enum class DocTestState {
        NONE, OPEN, CLOSED
    }

    private fun <IN : CtType<*>, OUT> typeMemo(fn: (IN) -> OUT): (IN) -> OUT {
        val cache: MutableMap<String, OUT> = HashMap()
        return {
            cache.getOrPut(it.simpleName) { fn(it) }
        }
    }
}
