package org.operatorfoundation.flower

import kotlinx.coroutines.*
import org.operatorfoundation.transmission.TransmissionConnection
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level
import java.util.logging.Logger

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    var readQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    var writeQueue: BlockingQueue<Message> = LinkedBlockingQueue()

    private val readParentJob = Job()
    private var readCouroutineScope = CoroutineScope(Dispatchers.Default + readParentJob)

    private val writeParentJob = Job()
    private var writeCoroutineScope = CoroutineScope(Dispatchers.Default + writeParentJob)

    init
    {
        println("FlowerConnection.init called.")
        readCouroutineScope.launch {
            readMessages()
        }


        writeCoroutineScope.launch {
            writeMessages()
        }
    }

    @Synchronized
    fun readMessage(): Message
    {
        return readQueue.take()
    }

    fun readMessages()
    {
        println("FlowerConnection.readMessages() called. Starting loop...")
        readCouroutineScope.async(Dispatchers.IO)
        {
            while (true)
            {
                val maybeData = connection.readWithLengthPrefix(16)

                if (maybeData == null)
                {
                    println("FlowerConnection.readMessages: failed to read data from the Transmission connection.")
                    logger?.log(Level.SEVERE, "Flower failed to read data from the Transmission connection.")
                    return@async
                }
                else
                {
                    println("FlowerConnection.readMessages: read some data: ${maybeData.decodeToString()}")
                    logger?.log(Level.FINE, "FlowerConnection.readMessages read some data: ${maybeData.decodeToString()}" )

                    val message = Message(maybeData)
                    readQueue.add(message)
                    println("FlowerConnection.readMessages: added a message to the queue - ${message.description}")
                }
            }
        }
    }

    @Synchronized
    fun writeMessage(message: Message)
    {
        println("FlowerConnection.writeMessage: adding a message to the queue: ${message.description}")
        writeQueue.put(message)
    }

    fun writeMessages()
    {
        println("FlowerConnection.writeMessages() called. Starting loop...")
        writeCoroutineScope.async(Dispatchers.IO)
        {
            while (true)
            {
                if (writeQueue.isNotEmpty())
                {
                    println("FlowerConnection.writeMessages: Found something in the queue.")

                    val message = writeQueue.remove()
                    val messageData = message.data

                    println("FlowerConnection.writeMessages: removed a message from the queue - ${message.description}.")

                    val messageSent = connection.writeWithLengthPrefix(messageData, 16)

                    if (messageSent == false)
                    {
                        println("FlowerConnection.writeMessages: failed to write a message.")
                        logger?.log(Level.SEVERE, "FlowerConnection.writeMessages: failed to write a message.")
                        return@async
                    }

                    println("FlowerConnection.writeMessages: sent a message to the transmission connection - ${message.description}")
                }
            }
        }
    }



}