package org.botlaxy.telegramit.core.handler.loader.compile

import mu.KotlinLogging
import org.botlaxy.telegramit.core.extension.getFileSystem
import org.botlaxy.telegramit.core.handler.dsl.Handler
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import javax.script.ScriptEngineFactory

private val logger = KotlinLogging.logger {}

class KotlinScriptCompiler : HandlerScriptCompiler {

    private val scriptEngineFactory: ScriptEngineFactory = KotlinJsr223JvmLocalScriptEngineFactory()

    override fun compile(file: Path): Handler {
        return try {
            logger.debug { "Compile $file" }
            val fileUri = file.toUri()
            if (fileUri.scheme == "file") {
                Files.newInputStream(file).bufferedReader().use {
                    scriptEngineFactory.scriptEngine.eval(it) as Handler
                }
            } else {
                fileUri.getFileSystem().use { fileSystem: FileSystem ->
                    Files.newInputStream(fileSystem.getPath(file.toString())).bufferedReader().use {
                        scriptEngineFactory.scriptEngine.eval(it) as Handler
                    }
                }
            }
        } catch (e: Exception) {
            throw HandlerCompileException("Can't compile ${file.fileName}", e)
        }
    }

}
