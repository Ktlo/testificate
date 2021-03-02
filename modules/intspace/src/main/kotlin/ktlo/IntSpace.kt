package ktlo

import java.util.*

class IntSpace : AbstractMutableSet<Int>() {
    private val tree = TreeMap<Int, Int>()

    override var size: Int = 0
        private set

    override fun add(element: Int): Boolean {
        val lower: Map.Entry<Int, Int>? = tree.floorEntry(element)
        val greater: Map.Entry<Int, Int>? = tree.higherEntry(element)
        when {
            lower != null && greater != null && lower.value + 1 == element && greater.key - 1 == element -> {
                tree.remove(greater.key)
                tree[lower.key] = greater.value
            }
            greater != null && greater.key - 1 == element -> {
                tree.remove(greater.key)
                tree[element] = greater.value
            }
            lower != null && element == lower.value + 1 -> tree[lower.key] = element
            lower != null && element <= lower.value -> return false
            else -> tree[element] = element
        }
        ++size
        return true
    }

    override operator fun contains(element: Int): Boolean {
        val lower: MutableMap.MutableEntry<Int, Int> = tree.floorEntry(element) ?: return false
        return element in lower.key..lower.value
    }

    override fun remove(element: Int): Boolean {
        val lower: Map.Entry<Int, Int> = tree.floorEntry(element) ?: return false
        element !in lower.key..lower.value && return false
        when (element) {
            lower.key -> {
                tree.remove(element)
                tree[element + 1] = lower.value
            }
            lower.value -> {
                tree[lower.key] = element - 1
            }
            else -> {
                tree[lower.key] = element - 1
                tree[element + 1] = lower.value
            }
        }
        --size
        return true
    }

    override fun clear() {
        size = 0
        tree.clear()
    }

    override fun isEmpty(): Boolean = tree.isEmpty()

    val rangeCount get() = tree.size

    fun ranges() = tree.asSequence().map { it.key..it.value }

    private class IntSpaceIterator(val iter: Iterator<Int>): MutableIterator<Int> {
        override fun hasNext() = iter.hasNext()
        override fun next() = iter.next()
        override fun remove() = throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<Int> = IntSpaceIterator(ranges().flatten().iterator())
}
