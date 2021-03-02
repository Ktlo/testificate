package ktlo.log.outputs

import ktlo.log.Level
import ktlo.log.Output
import java.time.Instant

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

    override fun write(level: Level, domain: String, message: Any?) {
        println("${Instant.now()} - ${level2str(level)} - [$domain]: $message")
    }
}
