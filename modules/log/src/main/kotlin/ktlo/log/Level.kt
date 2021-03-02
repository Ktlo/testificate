package ktlo.log

public enum class Level {
    None, Fatal, Error, Warning, Info, Debug, Trace;

    public companion object {
        private val byName = values().associateBy { it.name.toLowerCase() }

        public fun getByName(name: String): Level = byName[name.toLowerCase()] ?: error("not found")
    }
}
