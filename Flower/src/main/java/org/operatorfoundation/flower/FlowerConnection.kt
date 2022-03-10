package org.operatorfoundation.flower

import org.operatorfoundation.transmission.TransmissionConnection
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    var readQueue:Queue<Message> = LinkedList()
    var writeQueue: Queue<Message> = LinkedList()

    init
    {

    }

    fun readMessage(): Message?
    {
        if (readQueue.isEmpty())
        {
            return null
        }
        else
        {
            return readQueue.remove()
        }
    }

    fun readMessages()
    {
        while (true)
        {
            val maybeData = connection.readWithLengthPrefix(16)

            if (maybeData == null)
            {
                logger?.log(Level.SEVERE, "Flower failed to read data from the Transmission connection.")
                return
            }

            logger?.log(Level.FINE, "Flower read data: ${maybeData.decodeToString()}" )

            val message = Message(maybeData!!)
            readQueue.add(message)
        }
    }

    fun writeMessage(message: Message)
    {
        writeQueue.add(message)
    }

    fun writeMessages()
    {
        while (true)
        {
            if (writeQueue.isNotEmpty())
            {
                val message = writeQueue.remove()
                val messageData = message.data

                val messageSent = connection.writeWithLengthPrefix(messageData, 16)

                if (messageSent == false)
                {
                    logger?.log(Level.SEVERE, "Flower failed to write a message.")
                    return
                }
            }
        }
    }



}