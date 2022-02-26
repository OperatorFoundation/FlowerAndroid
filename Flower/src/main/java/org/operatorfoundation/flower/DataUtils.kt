package org.operatorfoundation.flower

import java.net.InetSocketAddress

fun ByteArray.splitOn(position: Int): Pair<ByteArray, ByteArray>
{
    val firstSlice = this.sliceArray(0 until position)
    val secondSlice = this.sliceArray(position until this.size)

    return Pair(firstSlice, secondSlice)
}