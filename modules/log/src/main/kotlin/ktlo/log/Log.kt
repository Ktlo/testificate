package ktlo.log

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import ktlo.log.outputs.ConsoleOutput
import ktlo.log.outputs.VoidOutput
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Объект журнала, ассоциированный с доменом.
 *
 * @property output используемая стратегия вывода сообщений журнала
 * @property domain домен этого объекта журнала
 * @property configuration заданная конфигурация для группы доменов, в которую входит объект журнала
 */
public class Log(
    private val output: Output,
    public val domain: String,
    public val configuration: Configuration
) {
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

    /**
     * Вывод сообщения в контексте этого объекта журналирования.
     *
     * В случае, когда вывод сообщения разрешён уровнем журнала,
     * получает сообщение, как результат функции [message].
     * Если заданный в конфигурации уровень меньше указанного [level],
     * то сообщение не будет построено и выведено.
     *
     * @param level уровень важности выводимого сообщения
     * @param message блок кода, конструирующий выводимое сообщение
     */
    public inline fun write(level: Level, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        if (level <= this.level) {
            writeInternal(level, message())
        }
    }

    /**
     * Вывод исключения [throwable] в контексте этого объекта журналирования с комментарием.
     *
     * Сообщение и комментарий не будут выведены, если уровень журнала меньше
     * указанного.
     * В случае, когда вывод разрешён уровнем журнала,
     * получает комментарий, как результат функции [message],
     * иначе сообщение не будет сконструировано вовсе.
     *
     * @param level уровень важности выводимого сообщения
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     */
    public inline fun throwable(level: Level, throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        if (level <= this.level) {
            throwableInternal(level, throwable, message())
        }
    }

    /**
     * Вывод исключения [throwable] в контексте этого объекта журналирования.
     *
     * Исключение не будет выведено, если уровень журнала меньше
     * указанного.
     *
     * @param level уровень важности выводимого сообщения
     * @param throwable выводимое исключение
     */
    public fun throwable(level: Level, throwable: Throwable) {
        if (level <= this.level) {
            throwableInternal(level, throwable)
        }
    }

    /**
     * Вывод сообщения уровня [Level.Trace].
     *
     * @param message блок кода, конструирующий выводимое сообщение
     * @see write
     */
    public inline fun trace(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        write(Level.Trace, message)
    }

    /**
     * Вывод сообщения уровня [Level.Debug].
     *
     * @param message блок кода, конструирующий выводимое сообщение
     * @see write
     */
    public inline fun debug(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        write(Level.Debug, message)
    }

    /**
     * Вывод сообщения уровня [Level.Debug].
     *
     * @param message блок кода, конструирующий выводимое сообщение
     * @see write
     */
    public inline fun info(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        write(Level.Info, message)
    }

    /**
     * Вывод сообщения уровня [Level.Warning].
     *
     * @param message блок кода, конструирующий выводимое сообщение
     * @see write
     */
    public inline fun warning(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        write(Level.Warning, message)
    }

    /**
     * Вывод сообщения уровня [Level.Error].
     *
     * @param message блок кода, конструирующий выводимое сообщение
     * @see write
     */
    public inline fun error(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        write(Level.Error, message)
    }

    @PublishedApi
    internal fun fatal(message: Any?): Nothing {
        if (Level.Fatal <= level) {
            writeInternal(Level.Fatal, message)
        }
        throw FatalError(message.toString())
    }

    /**
     * Вывод сообщения уровня [Level.Fatal].
     *
     * В отличии от других методов, отвечающих за вывод, этот метод,
     * независимо от уровня журналирования
     * данного объекта журналирования, всегда конструирует сообщение.
     * Полученное сообщение не будет отправлено в журнал, если уровень журнала
     * [Level.None], но будет являться частью иключения [FatalError], которое
     * гарантированно выбросит этот метод.
     *
     * Смысла делать [message] функцией не было, так как сообщение конструируется всегда,
     * но для идеоматичности это было сделано.
     *
     * @param message блок кода, конструирующий выводимое сообщение
     */
    public inline fun fatal(message: () -> Any?): Nothing {
        contract {
            callsInPlace(message, InvocationKind.EXACTLY_ONCE)
        }
        fatal(message())
    }

    /**
     * Вывод исключения уровня [Level.Trace].
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     * @see Log.throwable
     */
    public inline fun trace(throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        throwable(Level.Trace, throwable, message)
    }

    /**
     * Вывод исключения уровня [Level.Trace].
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
    public fun trace(throwable: Throwable) {
        throwable(Level.Trace, throwable)
    }

    /**
     * Вывод исключения уровня [Level.Debug].
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     * @see Log.throwable
     */
    public inline fun debug(throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        throwable(Level.Debug, throwable, message)
    }

    /**
     * Вывод исключения уровня [Level.Debug].
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
    public fun debug(throwable: Throwable) {
        throwable(Level.Debug, throwable)
    }

    /**
     * Вывод исключения уровня [Level.Info].
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     * @see Log.throwable
     */
    public inline fun info(throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        throwable(Level.Info, throwable, message)
    }

    /**
     * Вывод исключения уровня [Level.Info].
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
    public fun info(throwable: Throwable) {
        throwable(Level.Info, throwable)
    }

    /**
     * Вывод исключения уровня [Level.Warning].
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     * @see Log.throwable
     */
    public inline fun warning(throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        throwable(Level.Warning, throwable, message)
    }

    /**
     * Вывод исключения уровня [Level.Warning].
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
    public fun warning(throwable: Throwable) {
        throwable(Level.Warning, throwable)
    }

    /**
     * Вывод исключения уровня [Level.Error].
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     * @see Log.throwable
     */
    public inline fun error(throwable: Throwable, message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        throwable(Level.Error, throwable, message)
    }

    /**
     * Вывод исключения уровня [Level.Error].
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
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

    /**
     * Вывод исключения уровня [Level.Fatal].
     *
     * Комментарий к исключению будет сконструирован в любом случае. Он
     * будет использован как в журнале (если уровень журналирования не равен
     * [Level.None]), так и в новой ошибке [FatalError], как сообщение в свойстве
     * [FatalError.message]. Исключение [throwable] кроме записи в журнал будет
     * также передано ошибке [FatalError] в свойство [FatalError.cause].
     * Этот метод гарантированно выбросит ошибку [FatalError] в
     * любом случае.
     *
     * @param throwable выводимое исключение
     * @param message блок кода, конструирующий комментарий к исключению
     */
    public inline fun fatal(throwable: Throwable, message: () -> Any?): Nothing {
        contract {
            callsInPlace(message, InvocationKind.AT_LEAST_ONCE)
        }
        fatal(throwable, message())
    }

    /**
     * Вывод исключения уровня [Level.Fatal].
     *
     * Исключение [throwable] кроме записи в журнал будет
     * также передано ошибке [FatalError] в свойство [FatalError.cause].
     * Этот метод гарантированно выбросит ошибку [FatalError] в
     * любом случае.
     *
     * @param throwable выводимое исключение
     * @see Log.throwable
     */
    public fun fatal(throwable: Throwable): Nothing {
        if (Level.Fatal <= level) {
            throwableInternal(Level.Fatal, throwable)
        }
        throw FatalError(throwable)
    }

    /**
     * Уровень журналирования, до которого будут выводится сообщения.
     */
    public val level: Level get() = configuration.level

    public companion object {
        /**
         * Сообщения не выводятся и не конструируются этим журналом. Это значение
         * можно получить из свойства [current] при отсутствии журнала в текущем
         * контексте выполнения.
         */
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

        /**
         * Получить объект журналирования указаноого домена [domain].
         *
         * Этот метод создаёт объект журналирования, используя конфигурацию в файле
         * `META-INF/log.yaml` доступного классу [Log] загрузщику [ClassLoader].
         */
        public fun get(domain: String): Log {
            val configuration = confTree.get(domain)
            val output = outputs.getOrPut(configuration.output) {
                configuration.output.primaryConstructor!!.callBy(emptyMap())
            }
            return Log(output, domain, configuration)
        }

        @PublishedApi
        internal val _current: ThreadLocal<Log> = ThreadLocal()

        /**
         * Объект журналирования текущего контекста выполения.
         */
        public val current: Log get() = _current.get() ?: empty

        /**
         * Установить свойству [current] другой объект журналирования
         * для кода в [block] в текущес потоке.
         *
         * @param log замещающий объект журналирования
         * @param block блок кода, в котором будет действовать замещающий объект журналирования
         */
        public inline fun <R> using(log: Log, block: () -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }
            val previous = _current.get()
            try {
                _current.set(log)
                return block()
            } finally {
                if (previous == null) {
                    _current.remove()
                } else {
                    _current.set(previous)
                }
            }
        }

        /**
         * Вывод сообщения уровня [Level.Trace] текущим объектом журналирования [current].
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see write
         * @see current
         */
        public inline fun trace(message: () -> Any?): Unit = current.trace(message)

        /**
         * Вывод сообщения уровня [Level.Debug] текущим объектом журналирования [current].
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see write
         * @see current
         */
        public inline fun debug(message: () -> Any?): Unit = current.debug(message)

        /**
         * Вывод сообщения уровня [Level.Info] текущим объектом журналирования [current].
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see write
         * @see current
         */
        public inline fun info(message: () -> Any?): Unit = current.info(message)

        /**
         * Вывод сообщения уровня [Level.Warning] текущим объектом журналирования [current].
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see write
         * @see current
         */
        public inline fun warning(message: () -> Any?): Unit = current.warning(message)

        /**
         * Вывод сообщения уровня [Level.Error] текущим объектом журналирования [current].
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see write
         * @see current
         */
        public inline fun error(message: () -> Any?): Unit = current.error(message)

        /**
         * Вывод сообщения уровня [Level.Fatal] текущим объектом журналирования [current].
         *
         * В отличии от других методов, отвечающих за вывод, этот метод,
         * независимо от уровня журналирования
         * данного объекта журналирования, всегда конструирует сообщение.
         * Полученное сообщение не будет отправлено в журнал, если уровень журнала
         * [Level.None], но будет являться частью иключения [FatalError], которое
         * гарантированно выбросит этот метод.
         *
         * Смысла делать [message] функцией не было, так как сообщение конструируется всегда,
         * но для идеоматичности это было сделано.
         *
         * @param message блок кода, конструирующий выводимое сообщение
         * @see current
         */
        public inline fun fatal(message: () -> Any?): Nothing = current.fatal(message)

        /**
         * Вывод исключения уровня [Level.Trace] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see Log.throwable
         * @see current
         */
        public inline fun trace(throwable: Throwable, message: () -> Any?) {
            current.trace(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Trace] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun trace(throwable: Throwable) {
            current.trace(throwable)
        }

        /**
         * Вывод исключения уровня [Level.Debug] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see Log.throwable
         * @see current
         */
        public inline fun debug(throwable: Throwable, message: () -> Any?) {
            current.debug(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Debug] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun debug(throwable: Throwable) {
            current.debug(throwable)
        }

        /**
         * Вывод исключения уровня [Level.Info] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see Log.throwable
         * @see current
         */
        public inline fun info(throwable: Throwable, message: () -> Any?) {
            current.info(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Info] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun info(throwable: Throwable) {
            current.info(throwable)
        }

        /**
         * Вывод исключения уровня [Level.Warning] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see Log.throwable
         * @see current
         */
        public inline fun warning(throwable: Throwable, message: () -> Any?) {
            current.warning(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Warning] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun warning(throwable: Throwable) {
            current.warning(throwable)
        }

        /**
         * Вывод исключения уровня [Level.Error] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see Log.throwable
         * @see current
         */
        public inline fun error(throwable: Throwable, message: () -> Any?) {
            current.error(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Error] текущим объектом журналирования [current].
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun error(throwable: Throwable) {
            current.error(throwable)
        }

        /**
         * Вывод исключения уровня [Level.Fatal] текущим объектом журналирования [current].
         *
         * Комментарий к исключению будет сконструирован в любом случае. Он
         * будет использован как в журнале (если уровень журналирования не равен
         * [Level.None]), так и в новой ошибке [FatalError], как сообщение в свойстве
         * [FatalError.message]. Исключение [throwable] кроме записи в журнал будет
         * также передано ошибке [FatalError] в свойство [FatalError.cause].
         * Этот метод гарантированно выбросит ошибку [FatalError] в
         * любом случае.
         *
         * @param throwable выводимое исключение
         * @param message блок кода, конструирующий комментарий к исключению
         * @see current
         */
        public inline fun fatal(throwable: Throwable, message: () -> Any?): Nothing {
            current.fatal(throwable, message)
        }

        /**
         * Вывод исключения уровня [Level.Fatal] текущим объектом журналирования [current].
         *
         * Исключение [throwable] кроме записи в журнал будет
         * также передано ошибке [FatalError] в свойство [FatalError.cause].
         * Этот метод гарантированно выбросит ошибку [FatalError] в
         * любом случае.
         *
         * @param throwable выводимое исключение
         * @see Log.throwable
         * @see current
         */
        public fun fatal(throwable: Throwable): Nothing {
            current.fatal(throwable)
        }
    }
}

// Набор функций, аналогичный check и require

/**
 * Проверяет значение [value] на истинность и в случае неудачи
 * генерирует ошибку [FatalError] с записью в текущий объект журналирования
 * [Log.current] сообщения уровня [Level.Fatal], которое будет получено
 * вызовом функции [message].
 *
 * Сообщение не будет сконструировано, если значение [value] равно истине.
 *
 * @param value проверяемое на истинность значение какого-то выражения
 * @param message блок кода, конструирующий выводимое в журнал сообщение
 * @see Log.Companion.fatal
 */
public inline fun validate(value: Boolean, message: () -> Any?) {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        returns() implies value
    }
    if (!value) {
        Log.fatal(message)
    }
}

/**
 * Проверяет значение [value] на истинность и в случае неудачи
 * генерирует ошибку [FatalError] с регистрацией данного инцидента
 * в журнале, используя текущий объект журналирования. Соответствующее
 * сообщение журнала будет иметь уровень [Level.Fatal].
 *
 * @param value проверяемое на истинность значение какого-то выражения
 * @see Log.Companion.fatal
 */
public fun validate(value: Boolean) {
    contract {
        returns() implies value
    }
    validate(value) { "validation failed" }
}

/**
 * Проверяет, что переданное значение [value] не равно `null`.
 *
 * В случае успеха будет возвращено не `null` значение.
 *
 * Если значение равно `null`, то будет сгенерировано сообщение, как результат
 * функции [message], которое будет записано в журнал текущим объектом журналирования
 * [Log.current] с уровнем сообщения [Level.Fatal]. Затем будет выброшена ошибка
 * [FatalError].
 *
 * @param value проверяемое на не `null` значение
 * @param message блок кода, конструирующий выводимое в журнал сообщение
 * @return гарантировано не `null` переданное значение
 * @see Log.Companion.fatal
 */
public inline fun <T> validateNotNull(value: T?, message: () -> Any?): T {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        returns() implies (value != null)
    }
    validate(value != null, message)
    return value
}

/**
 * Проверяет, что переданное значение [value] не равно `null`.
 *
 * В случае успеха будет возвращено не `null` значение.
 *
 * Если значение равно `null`, то будет выброшена ошибка
 * [FatalError].
 *
 * @param value проверяемое на не `null` значение
 * @return гарантировано не `null` переданное значение
 * @see Log.Companion.fatal
 */
public fun <T> validateNotNull(value: T?): T {
    contract {
        returns() implies (value != null)
    }
    return validateNotNull(value) { "value was null" }
}

// Подзадачи

/**
 * Выполняет блок кода [block] в текущем потоке, добавляя
 * указанный в [name] уровень домена журналирования.
 *
 * Для этого заменяется текущий объект журналирования
 * [Log.current] для блока кода [block]. По выходу из
 * из этой функции предудущий домен и его объект журналирования
 * возвращаются на место
 *
 * @param name новый уровень домена журнала
 * @param block блок кода, выполняемый с указанным доменом журнала
 * @return значение, которое вернула функция [block]
 */
public inline fun <R> subprogram(name: String, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val log = Log.current
    return Log.using(Log.get(log.domain + '/' + name), block)
}

/**
 * Выполняет блок кода [block] с новым уровнем домена журнала [name].
 *
 * Предыдущий уровень домена берётся из объекта [CoroutineName] в текущем
 * контексте корутин. В новом контексте значение объекта [CoroutineName]
 * будет иметь значение нового домена журнала.
 *
 * Данная функция управляет значением [Log.current], тем самым гарантируя
 * правильную работу при переключении корутины между разными потоками.
 *
 * @param name новый уровень домена журнала
 * @param context контекст, который можно использовать, не вызывая для этого дополнительно [withContext]
 * @param block блок кода и поддерево корутин, выполняемых с указанным доменом журнала
 * @return значение, которое вернула функция [block]
 */
public suspend fun <R> branch(
    name: String,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val coroutineName = coroutineContext[CoroutineName]
    val branchName = if (coroutineName == null) {
        name
    } else {
        coroutineName.name + '/' + name
    }
    val newContext = context + CoroutineName(branchName) + Log._current.asContextElement(Log.get(branchName))
    return withContext(newContext, block)
}
