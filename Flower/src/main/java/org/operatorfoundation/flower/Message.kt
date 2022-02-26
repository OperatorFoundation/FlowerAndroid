package org.operatorfoundation.flower

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer

typealias StreamIdentifier = Long

open class Message(data: ByteArray)
{
    val messageType: MessageType
    val content: Content

    init
    {
        val (messageTypeSlice, tail) = data.splitOn(1)
        require(messageTypeSlice.isNotEmpty())
            {"Failed to create a message, we were unable to parse the message type."}

        val messageTypeInt = messageTypeSlice.first().toInt()
        messageType = MessageType.from(messageTypeInt)

        when(messageType)
        {
            MessageType.TCPOpenV4Type ->
            {
                val endpointSize = AddressSize.v4.size + 2
                val (destinationData, streamIDData) = tail.splitOn(endpointSize)

                require(destinationData.isNotEmpty())
                    {"Failed to create a TCPOpenV4 message, we were unable to parse the destination address."}
                require(streamIDData.isNotEmpty())
                    {"Failed to create a TCPOpenV4 message, we were unable to parse the streamID."}

                val endpointV4 = EndpointV4(destinationData)
                val streamID = ByteBuffer.wrap(streamIDData).long
                content = TCPOpenV4(endpointV4, streamID)
            }
            MessageType.TCPOpenV6Type ->
            {
                val endpointSize = AddressSize.v6.size + 2
                val (destinationData, streamIDData) = tail.splitOn(endpointSize)

                require(destinationData.isNotEmpty())
                    {"Failed to create a TCPOpenV6 message, we were unable to parse the destination address."}
                require(streamIDData.isNotEmpty())
                    {"Failed to create a TCPOpenV6 message, we were unable to parse the streamID."}

                val endpointV6 = EndpointV6(destinationData)
                val streamID = ByteBuffer.wrap(streamIDData).long
                content = TCPOpenV6(endpointV6, streamID)
            }
            MessageType.TCPCloseType ->
            {
                val streamID = ByteBuffer.wrap(tail).long
                content = TCPClose(streamID)
            }
            MessageType.TCPDataType ->
            {
                val endpointSize = AddressSize.v4.size + 2
                val (streamIDData, payload) = tail.splitOn(endpointSize)
                require(streamIDData.isNotEmpty())

                    {"Failed to create a TCPData message, we were unable to parse the streamID."}
                require(payload.isNotEmpty())
                    {"Failed to create a TCPData message, we were unable to parse the payload."}

                val streamID = ByteBuffer.wrap(streamIDData).long
                content = TCPData(streamID, payload)
            }
            MessageType.UDPDataV4Type ->
            {
                val endpointSize = AddressSize.v4.size + 2
                val (destinationData, payload) = tail.splitOn(endpointSize)

                require(destinationData.isNotEmpty())
                    {"Failed to create a UDPDataV4 message, we were unable to parse the destination address."}
                require(payload.isNotEmpty())
                    {"Failed to create a UDPDataV4 message, we were unable to parse the payload."}

                val endpointV4 = EndpointV4(destinationData)
                content = UDPDataV4(endpointV4, payload)
            }
            MessageType.UDPDataV6Type ->
            {
                val endpointSize = AddressSize.v6.size + 2
                val (destinationData, payload) = tail.splitOn(endpointSize)

                require(destinationData.isNotEmpty())
                    {"Failed to create a UDPDataV6 message, we were unable to parse the destination address."}
                require(payload.isNotEmpty())
                    {"Failed to create a UDPDataV6 message, we were unable to parse the payload."}

                val endpointV6 = EndpointV6(destinationData)
                content = UDPDataV6(endpointV6, payload)
            }
            MessageType.IPAssignV4Type ->
            {
                val inet4Address = Inet4Address.getByAddress(tail)
                content = IPAssignV4(inet4Address as Inet4Address)
            }
            MessageType.IPAssignV6Type ->
            {
                val inet6Address = Inet6Address.getByAddress(tail)
                content = IPAssignV6(inet6Address as Inet6Address)
            }
            MessageType.IPAssignDualStackType ->
            {
                val (ipv4Bytes, ipv6Bytes) = tail.splitOn(AddressSize.v4.size)

                require(ipv4Bytes.isNotEmpty())
                    {"Failed to create a IPAssignDualStack message, we were unable to parse the IPV4 address."}
                require(ipv6Bytes.isNotEmpty())
                    {"Failed to create a IPAssignDualStack message, we were unable to parse the IPV6 address."}

                val inet4Address = Inet4Address.getByAddress(ipv4Bytes) as Inet4Address
                val inet6Address = Inet6Address.getByAddress(ipv6Bytes) as Inet6Address
                content = IPAssignDualStack(inet4Address, inet6Address)
            }
            MessageType.IPDataV4Type ->
            {
                content = IPDataV4(tail)
            }
            MessageType.IPDataV6Type ->
            {
                content = IPDataV6(tail)
            }
            MessageType.IPRequestV4Type ->
            {
                content = IPRequestV4()
            }
            MessageType.IPRequestV6Type ->
            {
                content = IPRequestV6()
            }
            MessageType.IPRequestDualStackType ->
            {
                content = IPRequestDualStack()
            }
            MessageType.IPReuseV4Type ->
            {
                val inet4Address = Inet4Address.getByAddress(tail) as Inet4Address
                content = IPReuseV4(inet4Address)
            }
            MessageType.IPReuseV6Type ->
            {
                val inet6Address = Inet6Address.getByAddress(tail) as Inet6Address
                content = IPReuseV6(inet6Address)
            }
            MessageType.IPReuseDualStackType ->
            {
                val (ipv4Bytes, ipv6Bytes) = tail.splitOn(AddressSize.v4.size)

                require(ipv4Bytes.isNotEmpty())
                    {"Failed to create a IPReuseDualStackType message, we were unable to parse the IPV4 address"}
                require(ipv6Bytes.isNotEmpty())
                    {"Failed to create a IPReuseDualStackType message, we were unable to parse the IPV6 address"}

                val inet4Address = Inet4Address.getByAddress(ipv4Bytes) as Inet4Address
                val inet6Address = Inet6Address.getByAddress(ipv6Bytes) as Inet6Address
                content = IPReuseDualStackType(inet4Address, inet6Address)
            }
            MessageType.ICMPDataV4Type ->
            {
                val (ipv4Bytes, payload) = tail.splitOn(AddressSize.v4.size)

                require(ipv4Bytes.isNotEmpty())
                {"Failed to create a ICMPDataV4Type message, we were unable to parse the IPV4 address"}
                require(payload.isNotEmpty())
                {"Failed to create a ICMPDataV4Type message, we were unable to parse the payload"}

                val inet4Address = Inet4Address.getByAddress(ipv4Bytes) as Inet4Address
                content = ICMPDataV4Type(inet4Address, payload)
            }
            MessageType.ICMPDataV6Type ->
            {
                val (ipv6Bytes, payload) = tail.splitOn(AddressSize.v6.size)

                require(ipv6Bytes.isNotEmpty())
                {"Failed to create a ICMPDataV6Type message, we were unable to parse the IPV6 address"}
                require(payload.isNotEmpty())
                {"Failed to create a ICMPDataV6Type message, we were unable to parse the payload"}

                val inet6Address = Inet6Address.getByAddress(ipv6Bytes) as Inet6Address
                content = ICMPDataV6Type(inet6Address, payload)
            }
        }
    }
}

open class Content {}

class TCPOpenV4(val endpointV4: EndpointV4, val streamIdentifier: StreamIdentifier): Content()
{
    // TODO
}

class TCPOpenV6(val endpointV6: EndpointV6, val streamIdentifier: StreamIdentifier): Content()
{
    // TODO
}

class TCPClose(val streamIdentifier: StreamIdentifier): Content()
{
    // TODO
}

class TCPData(val streamIdentifier: StreamIdentifier, val payload: ByteArray): Content()
{
    // TODO
}

class UDPDataV4(val endpointV4: EndpointV4, val payload: ByteArray): Content()
{
    // TODO
}

class UDPDataV6(val endpointV6: EndpointV6, val payload: ByteArray): Content()
{
    // TODO
}

class IPAssignV4(val inet4Address: Inet4Address): Content()
{
    // TODO
}

class IPAssignV6(val inet6Address: Inet6Address): Content()
{
    // TODO
}

class IPAssignDualStack(val inet4Address: Inet4Address, val inet6Address: Inet6Address): Content()
{
    // TODO
}

class IPDataV4(bytes: ByteArray): Content()
{
    // TODO
}

class IPDataV6(bytes: ByteArray): Content()
{
    // TODO
}

class IPRequestV4: Content()
{
    // TODO
}

class IPRequestV6: Content()
{
    // TODO
}

class IPRequestDualStack: Content()
{
    // TODO
}

class IPReuseV4(inet4Address: Inet4Address): Content()
{

}

class IPReuseV6(inet6Address: Inet6Address): Content()
{

}

class IPReuseDualStackType(inet4Address: Inet4Address, inet6Address: Inet6Address): Content()
{

}

class ICMPDataV4Type(inet4Address: Inet4Address, bytes: ByteArray): Content()
{

}

class ICMPDataV6Type(inet6Address: Inet6Address, bytes: ByteArray): Content()
{

}

enum class MessageType(val byte: Int)
{
    TCPOpenV4Type(0),
    TCPOpenV6Type(1),
    TCPCloseType(2),
    TCPDataType(3),
    UDPDataV4Type(4),
    UDPDataV6Type(5),
    IPAssignV4Type(6),
    IPAssignV6Type(7),
    IPAssignDualStackType(8),
    IPDataV4Type(9),
    IPDataV6Type(10),
    IPRequestV4Type(11),
    IPRequestV6Type(12),
    IPRequestDualStackType(13),
    IPReuseV4Type(14),
    IPReuseV6Type(15),
    IPReuseDualStackType(16),
    ICMPDataV4Type(17),
    ICMPDataV6Type(18);

    companion object {
        fun from(findValue: Int): MessageType = MessageType.values().first { it.byte == findValue }
    }
}