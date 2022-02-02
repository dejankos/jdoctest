package io.github.dejankos.jdoctest.core

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BasicCoreTest {

    @Test
    fun `should compile and run all examples`() {
        assertDoesNotThrow {
            JDocTest()
                .processSources(
                    "../example/src/main/java/io/github/dejankos/valid",
                    CLASSPATH_ELEMENTS
                )
        }
    }

    @Test
    fun `should throw compile exception`() {
        assertThrows<CompileException> {
            JDocTest()
                .processSources(
                    "../example/src/main/java/io/github/dejankos/invalid/compile",
                    CLASSPATH_ELEMENTS
                )
        }
    }
}
