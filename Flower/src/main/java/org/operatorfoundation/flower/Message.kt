package org.operatorfoundation.flower

import java.net.Inet4Address
import java.net.Inet6Address
import java.nio.ByteBuffer

typealias StreamIdentifier = Long

open class Message(val data: ByteArray)
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

    val description: String
    get()
    {
        when(messageType)
        {
            MessageType.TCPOpenV4Type ->
            {
                val tcpOpenV4 = this.content as TCPOpenV4
                return """
                IPAssignV4
                IP: ${tcpOpenV4.endpointV4.host.hostAddress}:${tcpOpenV4.endpointV4.port}
                """
            }
            MessageType.TCPOpenV6Type ->
            {
                val tcpOpenV6 = this.content as TCPOpenV6
                return """
                IPAssignV4
                IP: ${tcpOpenV6.endpointV6.host.hostAddress}:${tcpOpenV6.endpointV6.port}
                """
            }
            MessageType.TCPCloseType ->
            {
                val message = this.content as TCPClose
                return """
                TCPClose
                StreamIdentifier: ${message.streamIdentifier}
                """
            }
            MessageType.TCPDataType ->
            {
                val message = this.content as TCPData
                return """
                TCPData
                streamIdentifier: ${message.streamIdentifier}
                data: ${message.payload}
                """
            }
            MessageType.UDPDataV4Type ->
            {
                val message = this.content as UDPDataV4
                return """
                UDPDataV4
                endpointV4: ${message.endpointV4.host.hostAddress}:${message.endpointV4.port}
                data: ${message.payload}
                """
            }
            MessageType.UDPDataV6Type ->
            {
                val message = this.content as UDPDataV6
                return """
                UDPDataV6
                endpointV6: ${message.endpointV6.host.hostAddress}:${message.endpointV6.port}
                data: ${message.payload}
                """
            }
            MessageType.IPAssignV4Type ->
            {
                val message = this.content as IPAssignV4
                return """
                IPAssignV4
                IP: ${message.inet4Address.hostAddress}
                """
            }
            MessageType.IPAssignV6Type ->
            {
                val message = content as IPAssignV6
                return """
                IPAssignV6
                ip: ${message.inet6Address.hostAddress}
                """
            }
            MessageType.IPAssignDualStackType ->
            {
                val message = content as IPAssignDualStack
                return """
                IPAssignDualStack
                IPv4: ${message.inet4Address.hostAddress}
                IPv6: ${message.inet6Address.hostAddress}
                """
            }
            MessageType.IPDataV4Type ->
            {
                val message = content as IPDataV4
                return """
                IPDataV4
                data: ${message.bytes}
                """
            }
            MessageType.IPDataV6Type ->
            {
                val message = content as IPDataV6
                return """
                IPDataV6
                data: ${message.bytes}
                """
            }
            MessageType.IPRequestV4Type ->
            {
                return """
                IPRequestV4
                """
            }
            MessageType.IPRequestV6Type ->
            {
                return """
                IPRequestV6
                """
            }
            MessageType.IPRequestDualStackType ->
            {
                return """
                IPRequestDualStack
                """
            }
            MessageType.IPReuseV4Type ->
            {
                val message = content as IPReuseV4
                return """
                IPReuseV4
                IP: ${message.inet4Address.hostAddress}
                """
            }
            MessageType.IPReuseV6Type ->
            {
                val message = content as IPReuseV6
                return """
                IPReuseV6
                ip: ${message.inet6Address.hostAddress}
                """
            }
            MessageType.IPReuseDualStackType ->
            {
                val message = content as IPReuseDualStackType
                return """
                IPReuseDualStack
                IPv4: ${message.inet4Address.hostAddress}
                IPv6: ${message.inet6Address.hostAddress}
                """
            }
            MessageType.ICMPDataV4Type ->
            {
                val message = content as ICMPDataV4Type
                return """
                ICMPDataV4
                IPv4: ${message.inet4Address.hostAddress}
                Data: ${message.bytes}
                """
            }
            MessageType.ICMPDataV6Type ->
            {
                val message = content as ICMPDataV6Type
                return """
                ICMPDataV6
                IPv6: ${message.inet6Address.hostAddress}
                Data: ${message.bytes}
                """
            }
        }
    }

//    var data: ByteArray = ByteArray(1)
//    get()
//    {
//        when (this.messageType)
//        {
//            MessageType.TCPOpenV4Type ->
//            {
//                val tcpOpenV4 = this.content as TCPOpenV4
//                val messageTypeBytes = ByteArray(1)
//                messageTypeBytes[0] = this.messageType.byte.toByte()
//                val endpointBytes = tcpOpenV4.endpointV4.getBytes()
//                val streamIDBytes = longToByteArray(tcpOpenV4.streamIdentifier)
//
//                return messageTypeBytes + endpointBytes + streamIDBytes
//            }
//            MessageType.TCPOpenV6Type ->
//            {
//                val tcpOpenV6 = this.content as TCPOpenV6
//                val messageTypeBytes = ByteArray(1)
//                messageTypeBytes[0] = this.messageType.byte.toByte()
//                val endpointBytes = tcpOpenV6.endpointV6.getBytes()
//                val streamIDBytes = longToByteArray(tcpOpenV6.streamIdentifier)
//
//                return messageTypeBytes + endpointBytes + streamIDBytes
//            }
//        }
//    }
}

open class Content {}
class TCPOpenV4(val endpointV4: EndpointV4, val streamIdentifier: StreamIdentifier): Content()
class TCPOpenV6(val endpointV6: EndpointV6, val streamIdentifier: StreamIdentifier): Content()
class TCPClose(val streamIdentifier: StreamIdentifier): Content()
class TCPData(val streamIdentifier: StreamIdentifier, val payload: ByteArray): Content()
class UDPDataV4(val endpointV4: EndpointV4, val payload: ByteArray): Content()
class UDPDataV6(val endpointV6: EndpointV6, val payload: ByteArray): Content()
class IPAssignV4(val inet4Address: Inet4Address): Content()
class IPAssignV6(val inet6Address: Inet6Address): Content()
class IPAssignDualStack(val inet4Address: Inet4Address, val inet6Address: Inet6Address): Content() {}
class IPDataV4(val bytes: ByteArray): Content()
class IPDataV6(val bytes: ByteArray): Content()
class IPRequestV4: Content()
class IPRequestV6: Content()
class IPRequestDualStack: Content()
class IPReuseV4(val inet4Address: Inet4Address): Content()
class IPReuseV6(val inet6Address: Inet6Address): Content()
class IPReuseDualStackType(val inet4Address: Inet4Address, val inet6Address: Inet6Address): Content()
class ICMPDataV4Type(val inet4Address: Inet4Address, val bytes: ByteArray): Content()
class ICMPDataV6Type(val inet6Address: Inet6Address, val bytes: ByteArray): Content()

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