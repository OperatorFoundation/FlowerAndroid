package org.operatorfoundation.flower

import kotlinx.coroutines.*
import org.operatorfoundation.transmission.TransmissionConnection
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    var readQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    var writeQueue: BlockingQueue<Message> = LinkedBlockingQueue()

    private val parentJob = Job()
    private var readCouroutineScope = CoroutineScope(Dispatchers.Default + parentJob)
    private var writeCoroutineScope = CoroutineScope(Dispatchers.Default + parentJob)

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
        println("FlowerConnection.writeMessage: adding a message to the queue: $message")
        writeQueue.put(message)
    }

    fun writeMessages()
    {
        println("FlowerConnection.writeMessages() called")
        writeCoroutineScope.async(Dispatchers.IO)
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
                        println("Flower failed to write a message.")
                        logger?.log(Level.SEVERE, "Flower failed to write a message.")
                        return@async
                    }

                    println("FlowerConnection.writeMessages() removed a message from the queue and sent it to the transmission connection: $message")
                }
            }
        }

    }



}