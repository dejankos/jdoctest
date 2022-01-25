package io.github.dejankos.jdoctest.core

open class JDocTestException(
    error: String,
    cause: Throwable? = null,
    private val content: String
) : RuntimeException(error, cause)

class ParseException(
    error: String,
    content: String
) : JDocTestException(error = error, content = content)

class CompileException(
    error: String,
    content: String,
    private val line: Int,
    private val source: String
) : JDocTestException(error = error, content = content)

class ExecutionException(
    error: String,
    cause: Throwable,
    content: String
) : JDocTestException(error, cause, content)
