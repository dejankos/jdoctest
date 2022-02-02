package io.github.dejankos.jdoctest.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompileErrorTest {

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
