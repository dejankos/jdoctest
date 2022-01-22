package io.github.dejankos.jdoctest.core

open class JDocTestException : RuntimeException()

data class ParseException(
    private val error: String
) : JDocTestException()

data class CompileException(
    private val error: String
) : JDocTestException()