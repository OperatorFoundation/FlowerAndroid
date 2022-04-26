package org.operatorfoundation.flower

import org.operatorfoundation.transmission.TransmissionConnection
import java.util.logging.Level
import java.util.logging.Logger

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    @Synchronized
    fun readMessage(): Message?
    {
        println("FlowerConnection.readMessage() called")

        val maybeData = connection.readWithLengthPrefix(16)
        println("FlowerConnection.readMessages: returned from connection.readWithLengthPrefix")

        if (maybeData == null)
        {
            println("FlowerConnection.readMessages: failed to read data from the Transmission connection.")
            logger?.log(Level.SEVERE, "Flower failed to read data from the Transmission connection.")
            return null
        }
        else
        {
            println("FlowerConnection.readMessages: read some data: ${maybeData.decodeToString()}")
            logger?.log(Level.FINE, "FlowerConnection.readMessages read some data: ${maybeData.decodeToString()}" )

            val message = Message(maybeData)
            return message
        }
    }

    @Synchronized
    fun writeMessage(message: Message)
    {
        println("FlowerConnection.writeMessage(message: ${message.messageType}) called")

        val messageData = message.data
        val messageSent = connection.writeWithLengthPrefix(messageData, 16)

        println("FlowerConnection.writeMessage: sent a message to the transmission connection. Success - $messageSent")

        if (messageSent == false)
        {
            println("FlowerConnection.writeMessage: failed to write a message.")
            logger?.log(Level.SEVERE, "FlowerConnection.writeMessage: failed to write a message.")
            throw Exception("FlowerConnection.writeMessage: failed to write a message.")
        }
        else
        {
            println("FlowerConnection.writeMessage: ${message.messageType} message sent")
        }
    }

}