package io.github.dejankos.jdoctest.core

import kotlin.random.Random

class JDocCompiler(
    private val docsTest: List<DocTestParser.DocTestClassData>
) {

    fun compile() {
        for (docTestClassData in docsTest) {
        }
    }

    private fun bindDocTestCode(classContext: DocTestParser.ClassContext, docTestCode: DocTestParser.DocTestCode) =
        """
            package ${classContext.classPackage}
            ${classContext.classImports.joinMultiline()}
            ${docTestCode.docTestImports.joinMultiline()}
            
            public class ${classContext.className}_JDocTest_${Random.nextBits(3)} implements Runnable {
                public void run() throws Exception {
                    ${docTestCode.docTestCode.joinMultiline()}
                }
            }
        """

    private fun List<String>.joinMultiline() = this.joinToString(separator = "\n") { it }
}
