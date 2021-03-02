package ktlo.test

import ktlo.IntSpace
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntSpaceTest {
    @Test
    fun intSpaceTest() {
        val intSpace = IntSpace()
        val space = intSpace as MutableSet<Int>
        assertEquals(0, intSpace.rangeCount)
        assertEquals(0, space.size)
        assertTrue { space.isEmpty() }
        assertTrue { space.add(9) }
        assertFalse { space.isEmpty() }
        assertEquals(1, intSpace.rangeCount)
        assertEquals(1, space.size)
        assertTrue { 9 in space }
        assertTrue { space.add(13) }
        assertFalse { space.add(13) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(2, space.size)
        assertTrue { 13 in space && 9 in space }
        assertTrue { space.add(14) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(3, space.size)
        assertTrue { 14 in space && 13 in space && 9 in space }
        assertTrue { space.add(12) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(4, space.size)
        assertTrue { 12 in space && 14 in space && 13 in space && 9 in space }
        space += 8
        assertEquals(2, intSpace.rangeCount)
        assertEquals(5, space.size)
        assertTrue { space.add(10) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(6, space.size)
        assertTrue { 11 !in space }
        assertTrue { space.add(11) }
        assertEquals(1, intSpace.rangeCount)
        assertEquals(7, space.size)
        assertTrue { 11 in space }

        assertFalse { space.remove(23) }
        space -= 23
        assertFalse { space.remove(0) }
        assertTrue { space.remove(11) }
        assertFalse { space.remove(11) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(6, space.size)
        assertTrue { space.remove(8) }
        assertEquals(2, intSpace.rangeCount)
        assertEquals(5, space.size)
        assertFalse { 8 in space }
        assertTrue { space.remove(10) }
        assertFalse { 10 in space }
        assertTrue { 9 in space }
        assertTrue { 12 in space }

        assertEquals(listOf(9, 12, 13, 14), space.toList())

        space.clear()
        assertTrue { space.isEmpty() }
    }
}
