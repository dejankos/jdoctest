package io.github.dejankos.jdoctest.core

import java.io.File

val CLASSPATH_ELEMENTS by lazy {
    TARGET_DEPS.plus(listOf(TARGET_CLASSES))
}

private const val TARGET_CLASSES = "../example/target/classes/"

private val TARGET_DEPS = File("../example/target/dependency")
    .listFiles()
    ?.map { it.absolutePath }
    ?: mutableListOf()
