package org.operatorfoundation.flower

import kotlinx.coroutines.*
import org.operatorfoundation.transmission.TransmissionConnection
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    var readQueue:Queue<Message> = LinkedList()
    var writeQueue: Queue<Message> = LinkedList()

    private val parentJob = Job()
    private var readCouroutineScope = CoroutineScope(Dispatchers.Default + parentJob)
    private var writeCoroutineScope = CoroutineScope(Dispatchers.Default + parentJob)

    init
    {
        print("@~>~~  Flower init called.")
        readCouroutineScope.launch {
            print("^^^^^^ Launching read coroutine")
            readMessages()
        }


        writeCoroutineScope.launch {
            print("^^^^^ Launching write coroutine")
            writeMessages()
        }
    }

    @Synchronized
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
        readCouroutineScope.async(Dispatchers.IO)
        {
            while (true)
            {
                print("<----- readMessages() called!")
                val maybeData = connection.readWithLengthPrefix(16)

                if (maybeData == null)
                {
                    logger?.log(Level.SEVERE, "Flower failed to read data from the Transmission connection.")
                    return@async
                }

                logger?.log(Level.FINE, "Flower read data: ${maybeData.decodeToString()}" )

                val message = Message(maybeData!!)
                readQueue.add(message)
            }
        }
    }

    @Synchronized
    fun writeMessage(message: Message)
    {
        print("Write queue is addeding a message: $message")
        writeQueue.add(message)
    }

    fun writeMessages()
    {
        print("-----> writeMessages() called!")
        writeCoroutineScope.async(Dispatchers.IO)
        {
            print("-----> writeMessages() async happening!")
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
                        return@async
                    }
                }
            }
        }

    }



}