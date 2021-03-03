package ktlo.log.outputs

import ktlo.log.Level
import ktlo.log.Output
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Реализация [Output], которая выводит все сообщения в stdout.
 */
public class ConsoleOutput : Output {
    private fun level2str(level: Level) = when (level) {
        Level.None -> "none   "
        Level.Fatal -> "fatal  "
        Level.Error -> "error  "
        Level.Warning -> "warning"
        Level.Info -> "info   "
        Level.Debug -> "debug  "
        Level.Trace -> "trace  "
    }

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    override fun write(level: Level, domain: String, message: Any?) {
        println("${LocalDateTime.now().format(formatter)} - ${level2str(level)} - [$domain]: $message")
    }
}
