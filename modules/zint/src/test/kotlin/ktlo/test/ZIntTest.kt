package ktlo.test

import io.ktor.utils.io.core.*
import ktlo.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ZIntTest {

    class Case<T>(val value: T, val coded: ByteArray) {
        operator fun component1() = value
        operator fun component2() = coded
    }

    private inline fun <R> ByteReadPacket.preview(block: Input.() -> R): R = copy().use(block)

    @Test
    fun s32() {
        listOf(
            Case(0, byteArrayOf(0)),
            Case(Int.MAX_VALUE, byteArrayOf(-2, -1, -1, -1, 15)),
            Case(Int.MIN_VALUE, byteArrayOf(-127, -128, -128, -128, 16)),
            Case(-456, byteArrayOf(-111, 7)),
            Case(-87895, byteArrayOf(-81, -35, 10)),
            Case(687, byteArrayOf(-34, 10)),
            Case(48677, byteArrayOf(-54, -8, 5)),
            Case(23, byteArrayOf(46)),
            Case(-10, byteArrayOf(21))
        ).forEachIndexed { i, (value, coded) ->
            val mark = "#$i"
            assertEquals(coded.size, sizeSZInt32(value), mark)
            val bytes = buildPacket {
                writeSZInt32(this, value)
            }
            assertEquals(coded.size.toLong(), bytes.remaining, mark)
            val intermediate = bytes.preview { ByteArray(coded.size) { readByte() } }
            assertEquals(coded.toList(), intermediate.toList(), mark)
            assertEquals(value, readSZInt32(bytes), mark)
        }
    }

    @Test
    fun s64() {
        listOf(
            Case(0L, byteArrayOf(0)),
            Case(Long.MAX_VALUE, byteArrayOf(-2, -1, -1, -1, -1, -1, -1, -1, -1, 1)),
            Case(Long.MIN_VALUE, byteArrayOf(-127, -128, -128, -128, -128, -128, -128, -128, -128, 2)),
            Case(-456L, byteArrayOf(-111, 7)),
            Case(-87895L, byteArrayOf(-81, -35, 10)),
            Case(687L, byteArrayOf(-34, 10)),
            Case(48677L, byteArrayOf(-54, -8, 5)),
            Case(23L, byteArrayOf(46)),
            Case(-10L, byteArrayOf(21))
        ).forEachIndexed { i, (value, coded) ->
            val mark = "#$i"
            assertEquals(coded.size, sizeSZInt64(value), mark)
            val bytes = buildPacket {
                writeSZInt64(this, value)
            }
            assertEquals(coded.size.toLong(), bytes.remaining, mark)
            val intermediate = bytes.preview { ByteArray(coded.size) { readByte() } }
            assertEquals(coded.toList(), intermediate.toList(), mark)
            assertEquals(value, readSZInt64(bytes), mark)
        }
    }

    @Test
    fun u32() {
        listOf(
            Case(UInt.MIN_VALUE, byteArrayOf(0)),
            Case(UInt.MAX_VALUE, byteArrayOf(-1, -1, -1, -1, 15)),
            Case(687u, byteArrayOf(-81, 5)),
            Case(48677u, byteArrayOf(-91, -4, 2)),
            Case(23u, byteArrayOf(23))
        ).forEachIndexed { i, (value, coded) ->
            val mark = "#$i"
            assertEquals(coded.size, sizeUZInt32(value), mark)
            val bytes = buildPacket {
                writeUZInt32(this, value)
            }
            assertEquals(coded.size.toLong(), bytes.remaining, mark)
            val intermediate = bytes.preview { ByteArray(coded.size) { readByte() } }
            assertEquals(coded.toList(), intermediate.toList(), mark)
            assertEquals(value, readUZInt32(bytes), mark)
        }
    }

    @Test
    fun u64() {
        listOf(
            Case(ULong.MIN_VALUE, byteArrayOf(0)),
            Case(ULong.MAX_VALUE, byteArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, 1)),
            Case(687uL, byteArrayOf(-81, 5)),
            Case(48677uL, byteArrayOf(-91, -4, 2)),
            Case(726387615323uL, byteArrayOf(-37, -52, -51, -128, -110, 21)),
            Case(23uL, byteArrayOf(23))
        ).forEachIndexed { i, (value, coded) ->
            val mark = "#$i"
            assertEquals(coded.size, sizeUZInt64(value), mark)
            val bytes = buildPacket {
                writeUZInt64(this, value)
            }
            assertEquals(coded.size.toLong(), bytes.remaining, mark)
            val intermediate = bytes.preview { ByteArray(coded.size) { readByte() } }
            assertEquals(coded.toList(), intermediate.toList(), mark)
            assertEquals(value, readUZInt64(bytes), mark)
        }
    }

    @Test
    fun errors() {
        run {
            val src = buildPacket {
                repeat(5) {
                    writeUByte(255u)
                }
            }
            val message = assertFailsWith<RuntimeException> {
                readSZInt32(src)
            }.message
            assertEquals("SZInt32 is too big", message)
        }
        run {
            val src = buildPacket {
                repeat(10) {
                    writeUByte(255u)
                }
            }
            val message = assertFailsWith<RuntimeException> {
                readSZInt64(src)
            }.message
            assertEquals("SZInt64 is too big", message)
        }
        run {
            val src = buildPacket {
                repeat(5) {
                    writeUByte(255u)
                }
            }
            val message = assertFailsWith<RuntimeException> {
                readUZInt32(src)
            }.message
            assertEquals("UZInt32 is too big", message)
        }
        run {
            val src = buildPacket {
                repeat(10) {
                    writeUByte(255u)
                }
            }
            val message = assertFailsWith<RuntimeException> {
                readUZInt64(src)
            }.message
            assertEquals("UZInt64 is too big", message)
        }
    }
}
