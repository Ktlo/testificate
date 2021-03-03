package ktlo.log.outputs

import ktlo.log.Level
import ktlo.log.Output

/**
 * Реализация [Output], которая никуда не выводит сообщения.
 */
public class VoidOutput : Output {
    override fun write(level: Level, domain: String, message: Any?) {}
    override fun throwable(level: Level, domain: String, throwable: Throwable, message: Any?) {}
    override fun throwable(level: Level, domain: String, throwable: Throwable) {}
}
