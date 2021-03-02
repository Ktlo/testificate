package ktlo.log

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import ktlo.log.outputs.ConsoleOutput
import ktlo.log.outputs.VoidOutput
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

public class Log(
    private val output: Output,
    public val domain: String,
    public val configuration: Configuration
) : CoroutineContext.Element {
    override val key: Key get() = Key

    @PublishedApi
    internal fun writeInternal(level: Level, message: Any?) {
        output.write(level, domain, message)
    }

    @PublishedApi
    internal fun throwableInternal(level: Level, throwable: Throwable, message: Any?) {
        output.throwable(level, domain, throwable, message)
    }

    @PublishedApi
    internal fun throwableInternal(level: Level, throwable: Throwable) {
        output.throwable(level, domain, throwable)
    }

    public inline fun write(level: Level, message: () -> Any?) {
        if (level <= this.level) {
            writeInternal(level, message())
        }
    }

    public fun throwable(level: Level, throwable: Throwable) {
        if (level <= this.level) {
            throwableInternal(level, throwable)
        }
    }

    public inline fun throwable(level: Level, throwable: Throwable, message: () -> Any?) {
        if (level <= this.level) {
            throwableInternal(level, throwable, message())
        }
    }

    public inline fun trace(message: () -> Any?) {
        write(Level.Trace, message)
    }

    public inline fun debug(message: () -> Any?) {
        write(Level.Debug, message)
    }

    public inline fun info(message: () -> Any?) {
        write(Level.Info, message)
    }

    public inline fun warning(message: () -> Any?) {
        write(Level.Warning, message)
    }

    public inline fun error(message: () -> Any?) {
        write(Level.Error, message)
    }

    @PublishedApi
    internal fun fatal(message: Any?): Nothing {
        if (Level.Fatal <= level) {
            writeInternal(Level.Fatal, message)
        }
        throw FatalError(message.toString())
    }

    public inline fun fatal(message: () -> Any?): Nothing {
        fatal(message())
    }

    public inline fun trace(throwable: Throwable, message: () -> Any?) {
        throwable(Level.Trace, throwable, message)
    }

    public fun trace(throwable: Throwable) {
        throwable(Level.Trace, throwable)
    }

    public inline fun debug(throwable: Throwable, message: () -> Any?) {
        throwable(Level.Debug, throwable, message)
    }

    public fun debug(throwable: Throwable) {
        throwable(Level.Debug, throwable)
    }

    public inline fun info(throwable: Throwable, message: () -> Any?) {
        throwable(Level.Info, throwable, message)
    }

    public fun info(throwable: Throwable) {
        throwable(Level.Info, throwable)
    }

    public inline fun warning(throwable: Throwable, message: () -> Any?) {
        throwable(Level.Warning, throwable, message)
    }

    public fun warning(throwable: Throwable) {
        throwable(Level.Warning, throwable)
    }

    public inline fun error(throwable: Throwable, message: () -> Any?) {
        throwable(Level.Error, throwable, message)
    }

    public fun error(throwable: Throwable) {
        throwable(Level.Error, throwable)
    }

    @PublishedApi
    internal fun fatal(throwable: Throwable, message: Any?): Nothing {
        if (Level.Fatal <= level) {
            throwableInternal(Level.Fatal, throwable, message)
        }
        throw FatalError(message.toString(), throwable)
    }

    public inline fun fatal(throwable: Throwable, message: () -> Any?): Nothing {
        fatal(throwable, message())
    }

    public fun fatal(throwable: Throwable): Nothing {
        if (Level.Fatal <= level) {
            throwableInternal(Level.Fatal, throwable)
        }
        throw FatalError(throwable)
    }

    public val level: Level get() = configuration.level

    public companion object Key : CoroutineContext.Key<Log> {
        public val empty: Log = Log(VoidOutput(), "", Configuration.silent)

        private val confTree: ConfNode

        init {
            val resource = this::class.java.classLoader.getResourceAsStream("META-INF/log.yaml")
            confTree = if (resource == null) {
                ConfNode(Level.Info, ConsoleOutput::class, emptyMap())
            } else {
                loadConfigurationTree(resource)
            }
        }

        private val outputs = mutableMapOf<KClass<out Output>, Output>()

        public fun get(domain: String): Log {
            val configuration = confTree.get(domain)
            val output = outputs.getOrPut(configuration.output) {
                configuration.output.primaryConstructor!!.callBy(emptyMap())
            }
            return Log(output, domain, configuration)
        }

        public suspend inline fun trace(message: () -> Any?): Unit = getLog().trace(message)
        public suspend inline fun debug(message: () -> Any?): Unit = getLog().debug(message)
        public suspend inline fun info(message: () -> Any?): Unit = getLog().info(message)
        public suspend inline fun warning(message: () -> Any?): Unit = getLog().warning(message)
        public suspend inline fun error(message: () -> Any?): Unit = getLog().error(message)
        public suspend inline fun fatal(message: () -> Any?): Nothing = getLog().fatal(message)

        public suspend inline fun trace(throwable: Throwable, message: () -> Any?) {
            getLog().trace(throwable, message)
        }

        public suspend inline fun trace(throwable: Throwable) {
            getLog().trace(throwable)
        }

        public suspend inline fun debug(throwable: Throwable, message: () -> Any?) {
            getLog().debug(throwable, message)
        }

        public suspend inline fun debug(throwable: Throwable) {
            getLog().debug(throwable)
        }

        public suspend inline fun info(throwable: Throwable, message: () -> Any?) {
            getLog().info(throwable, message)
        }

        public suspend inline fun info(throwable: Throwable) {
            getLog().info(throwable)
        }

        public suspend inline fun warning(throwable: Throwable, message: () -> Any?) {
            getLog().warning(throwable, message)
        }

        public suspend inline fun warning(throwable: Throwable) {
            getLog().warning(throwable)
        }

        public suspend inline fun error(throwable: Throwable, message: () -> Any?) {
            getLog().error(throwable, message)
        }

        public suspend inline fun error(throwable: Throwable) {
            getLog().error(throwable)
        }

        public suspend inline fun fatal(throwable: Throwable, message: () -> Any?): Nothing {
            getLog().fatal(throwable, message)
        }

        public suspend inline fun fatal(throwable: Throwable): Nothing {
            getLog().fatal(throwable)
        }
    }
}

public suspend inline fun getLog(): Log = coroutineContext[Log] ?: Log.empty

public suspend inline fun validate(value: Boolean, message: () -> Any?) {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        returns() implies value
    }
    if (!value) {
        Log.fatal(message)
    }
}

public suspend fun <R> branch(name: String, block: suspend CoroutineScope.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val coroutineName = coroutineContext[CoroutineName]
    val branchName = if (coroutineName == null) {
        name
    } else {
        coroutineName.name + '/' + name
    }
    return withContext(CoroutineName(branchName) + Log.get(branchName), block)
}
