package ktlo.log

import com.amihaiemil.eoyaml.Node
import com.amihaiemil.eoyaml.Yaml
import com.amihaiemil.eoyaml.YamlMapping
import ktlo.log.outputs.ConsoleOutput
import ktlo.log.outputs.VoidOutput
import java.io.InputStream
import java.io.Reader
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Конфигурация журнала. Используется для настройки объекта журнала [Log].
 *
 * @property domain настроеваемый этим объектом домен и его поддомены
 * @property level максимальный уровень сообщений, который могут быть выведены в журнал
 * @property output класс [Output], реализующий стратегию вывода сообщений для данного домена и его поддоменов
 */
public data class Configuration(
    val domain: String,
    val level: Level = Level.Info,
    val output: KClass<out Output>
) {
    public companion object {

        /**
         * Конфигурация, которая не выводит никуда сообщения
         */
        public val silent: Configuration = Configuration(domain = "", level = Level.None, VoidOutput::class)
    }
}

internal data class ConfNode(
    val level: Level,
    val output: KClass<out Output>,
    val next: Map<String, ConfNode>
) {
    internal fun get(domain: String): Configuration {
        var current = this
        val resolvedDomain = mutableListOf<String>()
        for (branch in domain.splitToSequence('/')) {
            current = current.next[branch] ?: break
            resolvedDomain += branch
        }
        return Configuration(
            domain = resolvedDomain.joinToString(separator = "/"),
            level = current.level,
            output = current.output
        )
    }
}

private val loader = Log::class.java.classLoader

private fun loadOutput(output: String): KClass<out Output> {
    val klass = loader.loadClass(output).kotlin
    check(klass.isSubclassOf(Output::class)) { "not an log output class: $output" }
    @Suppress("UNCHECKED_CAST")
    return klass as KClass<out Output>
}

// Тут много багов, я знаю
private fun loadNode(yaml: YamlMapping, defaultLevel: Level, defaultOutput: KClass<out Output>): ConfNode {
    val level = yaml.string("level")?.let { Level.getByName(it) } ?: defaultLevel
    val output = yaml.string("output")?.let { loadOutput(it) } ?: defaultOutput
    val next = mutableMapOf<String, ConfNode>()
    for (key in yaml.keys()) {
        if (key.type() == Node.SCALAR) {
            val value = yaml.value(key)
            if (value.type() == Node.MAPPING) {
                val path = key.asScalar().value().split('/')
                val node = loadNode(value.asMapping(), level, output)
                next[path.first()] = path.drop(1).asReversed().fold(node) { conf, branch ->
                    ConfNode(level, output, mapOf(branch to conf))
                }
            }
        }
    }
    return ConfNode(level, output, next)
}

internal fun loadConfigurationTree(input: InputStream): ConfNode {
    val yaml = Yaml.createYamlInput(input).readYamlMapping()
    return loadNode(yaml, Level.Info, ConsoleOutput::class)
}
