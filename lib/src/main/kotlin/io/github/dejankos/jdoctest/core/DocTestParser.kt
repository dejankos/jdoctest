package io.github.dejankos.jdoctest.core

import spoon.Launcher
import spoon.javadoc.internal.JavadocDescriptionElement
import spoon.javadoc.internal.JavadocInlineTag
import spoon.javadoc.internal.JavadocSnippet
import spoon.reflect.CtModel
import spoon.reflect.code.CtComment
import spoon.reflect.code.CtJavaDoc
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter

class DocTestParser(private val path: String) {

    internal fun extract() {
        val classes = buildModel().getElements(TypeFilter(CtClass::class.java))
        for (c in classes) {
            for (m in c.methods) {
                for (comment in m.comments) {
                    extractDocTest(comment)
                }
            }
        }
    }

    private fun extractDocTest(comment: CtComment) {
        // TODO checks
        if (!comment.isJavadoc()) {
            return
        }

        val elements = comment.asJavaDoc().javadocElements

        val imports = mutableListOf<String>()
        val code = mutableListOf<String>()
        for (jd in elements.docTestCodeSeq()) {
            for (l in jd.lines()) {
                if (l.contains("import")) {
                    imports += l
                }
                else {
                    code += l
                }
                // todo new line
                // todo state per jd
            }

            println("imports")
            println(imports)
            println("code")
            println(code)
        }
    }

    private fun buildModel(): CtModel {
        val spoon = Launcher()
        spoon.addInputResource(path)
        return spoon.buildModel()
    }

    private fun CtComment.hadDocTest() = this.content.contains("jdoctest")

    private fun CtComment.isJavadoc() = this is CtJavaDoc

    private fun List<JavadocDescriptionElement>.docTestCodeSeq() = sequence<String> {
        var state = DocTestState.NONE
        for (e in this@docTestCodeSeq) {
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
                    // TODO wrong tags
                }
                is JavadocInlineTag -> {
                    if (state == DocTestState.OPEN && e.type == JavadocInlineTag.Type.CODE) {
                        yield(e.content)
                    }
                }
            }
        }
    }

    private enum class DocTestState {
        NONE, OPEN, CLOSED
    }
}

data class DocCode(
    val docTestImports: List<String>,
    val docTestCode: String
)
