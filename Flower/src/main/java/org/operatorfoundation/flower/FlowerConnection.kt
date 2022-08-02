package org.operatorfoundation.flower

import org.operatorfoundation.transmission.TransmissionConnection
import java.util.logging.Level
import java.util.logging.Logger

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?, val readLogging: Boolean = false, val writeLogging: Boolean = false)
{
    val readLock = Object()
    val writeLock = Object()

    var readLog: ArrayList<ByteArray>?
    var writeLog: ArrayList<ByteArray>?

    init {
        if (readLogging)
        {
            readLog = ArrayList<ByteArray>()
        }
        else
        {
            readLog = null
        }

        if (writeLogging)
        {
            writeLog = ArrayList<ByteArray>()
        }
        else
        {
            writeLog = null
        }
    }

    fun readMessage(): Message?
    {
        synchronized(readLock)
        {
            println("FlowerConnection.readMessage() called")

            val maybeData = connection.readWithLengthPrefix(16)
            println("FlowerConnection.readMessages: returned from connection.readWithLengthPrefix()")

            if (maybeData == null)
            {
                println("FlowerConnection.readMessage: failed to read data from the Transmission connection.")
                logger?.log(Level.SEVERE, "FlowerConnection.readMessage: failed to read data from the Transmission connection.")
                return null
            }
            else
            {
                println("FlowerConnection.readMessage: read some data: ${maybeData.decodeToString()}")
                logger?.log(
                    Level.FINE,
                    "FlowerConnection.readMessage: read some data: ${maybeData.decodeToString()}"
                )

                readLog?.add(maybeData)

                return Message(maybeData)
            }
        }
    }

    fun writeMessage(message: Message)
    {
       synchronized(writeLock)
       {
           println("FlowerConnection.writeMessage(message: ${message.messageType}) called")

           val messageData = message.data
           println("\"FlowerConnection.writeMessage: message size - ${messageData.count()}")
           println("\"FlowerConnection.writeMessage: message hex - ${messageData.toHexString()}")

           val messageSent = connection.writeWithLengthPrefix(messageData, 16)

           writeLog?.add(messageData)

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

               println("Write log: ")

               writeLog?.forEach {
                   println("Data Length ${it.size}: ${it.toHexString()}")
               }
           }
       }
    }

}