package org.operatorfoundation.flower

import org.junit.Test

import org.junit.Assert.*
import org.operatorfoundation.transmission.ConnectionType
import org.operatorfoundation.transmission.TransmissionConnection
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FlowerUnitTest
{
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testServerUDP()
    {
        val newPacketString = "45000021cbcb0000401100007f0000017f000001de1b04d2000dfe20746573740a"
        val pingPacket = hexStringToByteArray(newPacketString)

        val host = ""
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