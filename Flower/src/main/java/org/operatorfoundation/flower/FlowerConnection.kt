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

    private var readCoroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private var writeCoroutineScope = CoroutineScope(Dispatchers.IO + Job())

    init
    {
        println("FlowerConnection.init called.")

        writeCoroutineScope.launch {

            coroutineScope {
                launch { writeMessages() }
                launch { readMessages() }

            }
        }
    }

    @Synchronized
    fun readMessage(): Message
    {
        println("FlowerConnection.readMessage() called")
        return readQueue.take()
    }

    suspend fun readMessages() = coroutineScope {
        println("FlowerConnection.readMessages() called. Starting loop...")

        launch {
            while (true)
            {
//                println("~")
                val maybeData = withContext(coroutineContext) {
                    println("FlowerConnection.readMessages: calling connection.readWithLengthPrefix")
                    connection.readWithLengthPrefix(16)
                }

                if (maybeData == null)
                {
                    println("FlowerConnection.readMessages: failed to read data from the Transmission connection.")
                    logger?.log(Level.SEVERE, "Flower failed to read data from the Transmission connection.")
                    return@launch
                }
                else
                {
                    println("FlowerConnection.readMessages: read some data: ${maybeData.decodeToString()}")
                    logger?.log(Level.FINE, "FlowerConnection.readMessages read some data: ${maybeData.decodeToString()}" )

                    val message = Message(maybeData)
                    readQueue.add(message)
                    println("FlowerConnection.readMessages: added a message to the queue - ${message.messageType}")
                }
            }
        }
    }

    @Synchronized
    fun writeMessage(message: Message)
    {
        println("FlowerConnection.writeMessage: adding a message to the queue: ${message.messageType}")
        writeQueue.put(message)
    }

    suspend fun writeMessages() = coroutineScope {
        println("FlowerConnection.writeMessages() called. Starting loop...")

        launch {
            while (true)
            {
//                println("*")
                if (writeQueue.isNotEmpty())
                {
                    println("FlowerConnection.writeMessages: Found something in the queue.")

                    val message = writeQueue.remove()
                    val messageData = message.data

                    println("FlowerConnection.writeMessages: removed a message from the queue - ${message.messageType}.")

                    val messageSent = withContext(coroutineContext){
                        connection.writeWithLengthPrefix(messageData, 16)
                    }

                    println("FlowerConnection.writeMessages: sent a message to the transmission connection. Success - $messageSent")

                    if (messageSent == false)
                    {
                        println("FlowerConnection.writeMessages: failed to write a message.")
                        logger?.log(Level.SEVERE, "FlowerConnection.writeMessages: failed to write a message.")
                        return@launch
                    }
                    else
                    {
                        println("FlowerConnection.writeMessages: ${message.messageType} message sent")
                    }
                }
            }
        }
    }



}