package io.github.dejankos.jdoctest.core

class JDocTest {

    fun run(path: String) {

        val extract = DocTestParser(path).extract()
        JDocCompiler(extract).runAll()
    }
}
