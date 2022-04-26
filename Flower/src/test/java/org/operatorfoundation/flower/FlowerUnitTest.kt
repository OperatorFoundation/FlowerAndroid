package org.operatorfoundation.flower

import org.junit.Assert.fail
import org.junit.Test
import org.operatorfoundation.transmission.ConnectionType
import org.operatorfoundation.transmission.TransmissionConnection
import java.net.InetAddress

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FlowerUnitTest
{
    @Test
    fun testFlowerMessages()
    {
        try
        {
            // FIXME: Valid server IP
            val transmissionConnection = TransmissionConnection("", 1234, ConnectionType.TCP, null)
            val flowerConnection = FlowerConnection(transmissionConnection, null)
            val messageData = IPRequestV4().data
            val ipRequest = Message(messageData)

            println("\n🌙 is attempting to write a 🌻 message...")
            flowerConnection.writeMessage(ipRequest)

            println("\n🌙 is attempting to read a 🌻 message...")
            val flowerResponse = flowerConnection.readMessage()
            if (flowerResponse == null)
            {
                println("\uD83C\uDF19 The flower response was null.")
            }
            else
            {
                when(flowerResponse.messageType)
                {
                    MessageType.IPAssignV4Type ->
                    {
                        println("\n🌙 Got a 🌻 IPV4 Assignment!!")
                        val messageContent = flowerResponse.content as IPAssignV4
                        val inet4AddressData = messageContent.inet4Address.address
                        val inetAddress = InetAddress.getByAddress(inet4AddressData)
                        val ipv4AssignmentString = inetAddress.toString()
                    }
                    else ->
                    {
                        println("🌙 Our first response from the server was not an ipv4 assignment.")
                    }
                }
            }
        }
        catch (error: Exception)
        {
            println("🌙 Error creating socket: " + error.message)
        }
    }

    @Test
    fun testServerUDP()
    {
        val newPacketString = "450000258ad100004011ef41c0a801e79fcb9e5adf5104d200115d4268656c6c6f6f6f6f0a"
        val pingPacket = hexStringToByteArray(newPacketString)

        val host = "ServerIP"
        val port = 1234
        val transmissionConnection = TransmissionConnection(host, port, ConnectionType.TCP, null)
        val flowerConnection = FlowerConnection(transmissionConnection, null)
        
        // IP Request
        val messageData = IPRequestV4().data
        val ipRequest = Message(messageData)
        flowerConnection.writeMessage(ipRequest)

        val ipAssign = flowerConnection.readMessage()

        if (ipAssign == null)
        {
            fail()
        }
        else
        {
            when(ipAssign.messageType)
            {
                MessageType.IPAssignV4Type ->
                {
                    val messageContent = ipAssign.content as IPAssignV4
                    val inet4AddressData = messageContent.inet4Address.address

                    // Some hackery to give the server our assigned IP
                    pingPacket[15] = inet4AddressData[3]
                    pingPacket[14] = inet4AddressData[2]
                    pingPacket[13] = inet4AddressData[1]
                    pingPacket[12] = inet4AddressData[0]

                }
                else -> { fail() }
            }

            val message = Message(IPDataV4(pingPacket).data)
            flowerConnection.writeMessage(message)
        }
    }

    fun hexStringToByteArray(hexString: String): ByteArray
    {
        check(hexString.length % 2 == 0) { "Must be divisible by 2" }

        return hexString.chunked(2) // create character pairs
            .map { it.toInt(16).toByte() } // Convert pairs to integer
            .toByteArray() // Convert ints to a ByteArray
    }

}