package ktlo.log

public interface Output {
    public fun write(level: Level, domain: String, message: Any?)
    public fun throwable(level: Level, domain: String, throwable: Throwable, message: Any?) {
        write(level, domain, message.toString() + ": " + throwable.stackTraceToString())
    }
    public fun throwable(level: Level, domain: String, throwable: Throwable) {
        throwable(level, domain, throwable, "error occurred")
    }
}
