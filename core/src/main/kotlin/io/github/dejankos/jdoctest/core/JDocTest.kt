package io.github.dejankos.jdoctest.core

import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class JDocTest {
    private val log = LoggerFactory.getLogger(this::class.java)

    @OptIn(ExperimentalTime::class)
    fun processSources(path: String, classpathElements: List<String> = emptyList()) {
        log.info("Running JDocTest on source path $path")
        val (ctx, parseDuration) = measureTimedValue { DocTestParser(path).extract() }
        log.info("Sources parsed in ${parseDuration.inWholeSeconds} sec")
        val (_, runDuration) = measureTimedValue { JDocCompiler(ctx, classpathElements).runAll() }
        log.info("Sources compiled and run in ${runDuration.inWholeSeconds} sec")
        log.info("JDocTest successfully completed")
    }
}
