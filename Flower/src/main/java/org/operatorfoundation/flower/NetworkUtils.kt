package org.operatorfoundation.flower

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.nio.ByteBuffer

enum class AddressSize(val size:Int)
{
    v4(4),
    v6(16)
}

class EndpointV4
{
    val host: Inet4Address
    val port: Int

    constructor(host: Inet4Address, port: Int)
    {
        this.host = host
        this.port = port
    }

    constructor(bytes: ByteArray)
    {
        val (portBytes, tail) = bytes.splitOn(2)
        port = ByteBuffer.wrap(portBytes).short.toInt()
        host = Inet4Address.getByAddress(tail) as Inet4Address
    }

    fun getBytes(): ByteArray
    {
        val hostBytes: ByteArray = host.address
        var portBytes: ByteArray = ByteArray(2)
        portBytes[0] = port.toShort().toByte() // FIXME

        return  portBytes + hostBytes
    }

}

class EndpointV6
{
    val host: Inet6Address
    val port: Int

    constructor(host: Inet6Address, port: Int)
    {
        this.host = host
        this.port = port
    }

    constructor(bytes: ByteArray)
    {
        val (portBytes, tail) = bytes.splitOn(2)
        port = ByteBuffer.wrap(portBytes).short.toInt()
        host = Inet6Address.getByAddress(tail) as Inet6Address
    }

    fun getBytes(): ByteArray
    {
        val hostBytes: ByteArray = host.address
        var portBytes: ByteArray = ByteArray(2)
        portBytes[0] = port.toShort().toByte() // FIXME

        return  portBytes + hostBytes
    }

}