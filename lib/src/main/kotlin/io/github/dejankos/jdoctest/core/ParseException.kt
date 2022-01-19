package io.github.dejankos.jdoctest.core

data class ParseException(
    private val error: String
) : JDocTestException()
