package org.operatorfoundation.flower

import org.operatorfoundation.transmission.TransmissionConnection
import java.util.logging.Level
import java.util.logging.Logger

class FlowerConnection(var connection: TransmissionConnection, val logger: Logger?)
{
    init
    {

    }

    fun readMessage(): Message?
    {
        return null
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

            val maybeMessage = Message(maybeData!!)

        }
    }
}