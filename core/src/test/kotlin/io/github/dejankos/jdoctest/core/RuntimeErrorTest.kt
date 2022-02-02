package io.github.dejankos.jdoctest.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RuntimeErrorTest {

    @Test
    fun `should throw execution exception`() {
        assertThrows<ExecutionException> {
            JDocTest()
                .processSources(
                    "../example/src/main/java/io/github/dejankos/invalid/runtime",
                    CLASSPATH_ELEMENTS
                )
        }
    }
}
