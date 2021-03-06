package org.operatorfoundation.flower

import java.nio.ByteBuffer

fun ByteArray.splitOn(position: Int): Pair<ByteArray, ByteArray>
{
    val firstSlice = this.sliceArray(0 until position)
    val secondSlice = this.sliceArray(position until this.size)

    return Pair(firstSlice, secondSlice)
}

fun longToByteArray(value: Long): ByteArray
{
    val bytes = ByteArray(8)
    val byteBuffer = ByteBuffer.wrap(bytes).putLong(value)
    return byteBuffer.array()
}

fun ByteArray.toHexString() = asUByteArray().joinToString("") {
    it.toString(16).padStart(2, '0')
}