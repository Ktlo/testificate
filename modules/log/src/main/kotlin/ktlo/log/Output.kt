package ktlo.log

/**
 * Стратегия вывода сообщений журнала. Так как сообщение не обязательно является строкой,
 * реализация данного интерфейса может выводить сообщения в каком-нибудь другом виде.
 */
public interface Output {

    /**
     * Вывести сообщение с указанным доменом и уровнем журнала.
     */
    public fun write(level: Level, domain: String, message: Any?)

    /**
     * Вывести исключение с комментарием к нему с указанным доменом и уровнем журнала.
     */
    public fun throwable(level: Level, domain: String, throwable: Throwable, message: Any?) {
        write(level, domain, message.toString() + ": " + throwable.stackTraceToString())
    }

    /**
     * Вывести исключение с указанным доменом и уровнем журнала.
     */
    public fun throwable(level: Level, domain: String, throwable: Throwable) {
        throwable(level, domain, throwable, "error occurred")
    }
}
