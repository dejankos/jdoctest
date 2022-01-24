package io.github.dejankos.jdoctest.core

open class JDocTestException(
    error: String,
    cause: Throwable? = null
) : RuntimeException(error, cause)

class ParseException(
    error: String
) : JDocTestException(error)

class CompileException(
    error: String,
    private val line: Int,
    private val source: String
) : JDocTestException(error)

class ExecutionException(
    error: String,
    cause: Throwable
) : JDocTestException(error, cause)
