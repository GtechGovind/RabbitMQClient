package com.gtech.client

import com.rabbitmq.client.*
import kotlinx.coroutines.*

/**
 * A client for interacting with RabbitMQ.
 * This class provides connection management, message sending and receiving, and exchange and queue declaration.
 * The client supports automatic reconnection on failure.
 */
class RabbitMQClient private constructor(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val virtualHost: String,
    private val automaticReconnect: Boolean,
    private val reconnectDelay: Long,
    private val logger: (String) -> Unit
) {

    private val connectionFactory: ConnectionFactory = ConnectionFactory().apply {
        this.host = this@RabbitMQClient.host
        this.username = this@RabbitMQClient.username
        this.password = this@RabbitMQClient.password
        this.virtualHost = this@RabbitMQClient.virtualHost
    }

    private var connection: Connection? = null
    private var channel: Channel? = null

    private suspend fun init() {
        connect()
    }

    /**
     * Establishes a connection to RabbitMQ and creates a channel.
     * If the connection fails, it will trigger reconnection logic if enabled.
     */
    private suspend fun connect() {
        try {
            // Establish connection to RabbitMQ server
            connection = connectionFactory.newConnection()
            // Create a communication channel
            channel = connection!!.createChannel()
            // Register a listener for connection shutdown to handle disconnections
            connection!!.addShutdownListener { cause ->
                runBlocking { handleConnectionShutdown(cause) }
            }
            logger("Successfully connected to RabbitMQ at $host:$port")
        } catch (e: Exception) {
            logger("Failed to connect: ${e.message}. Retrying...")
            if (automaticReconnect) {
                reconnect()
            } else {
                throw e
            }
        }
    }

    /**
     * Handles connection shutdown scenarios and triggers reconnection if enabled.
     */
    private suspend fun handleConnectionShutdown(cause: ShutdownSignalException) {
        logger("Connection lost due to: ${cause.message}. Attempting reconnection...")
        connection?.removeShutdownListener {
            logger("Connection shut down")
        }
        if (automaticReconnect) {
            reconnect()
        }
    }

    /**
     * Attempts to reconnect to RabbitMQ with a delay between each attempt.
     * Will try reconnecting up to 5 times before giving up.
     */
    private suspend fun reconnect() {
        withContext(Dispatchers.IO) {
            var retryCount = 0
            while (retryCount < 5 && isActive) {
                try {
                    delay(reconnectDelay)
                    connect() // Attempt to reconnect
                    logger("Reconnected successfully to RabbitMQ.")
                    break
                } catch (e: Exception) {
                    logger("Reconnection attempt $retryCount failed: ${e.message}")
                    retryCount++
                }
            }
        }
    }

    /**
     * Declares an exchange in RabbitMQ with specified configurations.
     * Exchange types can be direct, fanout, topic, or headers.
     * @param exchangeName The name of the exchange to declare.
     * @param exchangeType The type of the exchange (e.g., "direct", "fanout").
     * @param durable Whether the exchange should survive server restarts.
     * @param autoDelete Whether the exchange should be deleted when no consumers are connected.
     */
    suspend fun declareExchange(
        exchangeName: String,
        exchangeType: BuiltinExchangeType,
        durable: Boolean = true,
        autoDelete: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            try {
                channel?.exchangeDeclare(exchangeName, exchangeType, durable, autoDelete, null)
                logger("Exchange declared: $exchangeName (Type: $exchangeType, Durable: $durable, AutoDelete: $autoDelete)")
            } catch (e: Exception) {
                logger("Error declaring exchange '$exchangeName': ${e.message}")
            }
        }
    }

    /**
     * Declares a queue in RabbitMQ with optional TTL (Time-To-Live) and expiration settings.
     * @param queueName The name of the queue to declare.
     * @param messageTTLInDays The TTL for messages in the queue (in days). Default is 0 (no TTL).
     * @param queueExpiresInYears The expiration time for the queue (in years). Default is 0 (no expiration).
     * @param durable Whether the queue should survive server restarts.
     * @param autoDelete Whether the queue should be deleted when no consumers are connected.
     */
    suspend fun declareQueueWithTTL(
        queueName: String,
        messageTTLInDays: Long = 0,
        queueExpiresInYears: Long = 0,
        durable: Boolean = true,
        autoDelete: Boolean = false
    ) {
        val messageTTLMillis = messageTTLInDays * 24 * 60 * 60 * 1000 // Convert days to milliseconds
        val queueExpiresMillis = queueExpiresInYears * 365 * 24 * 60 * 60 * 1000 // Convert years to milliseconds

        val arguments = mutableMapOf<String, Any>()

        if (messageTTLInDays > 0) {
            arguments["x-message-ttl"] = messageTTLMillis
        }

        if (queueExpiresInYears > 0) {
            arguments["x-expires"] = queueExpiresMillis
        }

        withContext(Dispatchers.IO) {
            try {
                // Declare the queue with optional TTL and expiration
                channel?.queueDeclare(queueName, durable, false, autoDelete, arguments)
                logger("Queue declared: $queueName (TTL: $messageTTLInDays days, Expires: $queueExpiresInYears years, Durable: $durable, AutoDelete: $autoDelete)")
            } catch (e: Exception) {
                logger("Error declaring queue '$queueName': ${e.message}")
            }
        }
    }

    /**
     * Sends a message to a specified exchange with a routing key.
     * The message is routed based on the exchange's configuration and the provided routing key.
     * @param exchangeName The name of the exchange to send the message to.
     * @param routingKey The routing key for the message.
     * @param message The message body.
     */
    suspend fun sendMessage(exchangeName: String, routingKey: String, message: String) {
        withContext(Dispatchers.IO) {
            try {
                // Publish the message to the exchange with the provided routing key
                channel?.basicPublish(exchangeName, routingKey, null, message.toByteArray())
                logger("Message sent to Exchange: $exchangeName with Routing Key: $routingKey. Message: $message")
            } catch (e: Exception) {
                logger("Error sending message to exchange '$exchangeName': ${e.message}")
            }
        }
    }

    /**
     * Starts consuming messages from a specified queue.
     * When a message is received, it will be passed to the provided handler function.
     * @param queueName The name of the queue to consume messages from.
     * @param handler The function to handle each received message.
     */
    suspend fun consumeMessages(queueName: String, handler: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val consumer = object : DefaultConsumer(channel) {
                    override fun handleDelivery(
                        consumerTag: String?,
                        envelope: Envelope?,
                        properties: AMQP.BasicProperties?,
                        body: ByteArray?
                    ) {
                        val message = String(body ?: ByteArray(0))
                        handler(message) // Pass the message to the handler
                    }
                }
                // Start consuming messages from the queue
                channel?.basicConsume(queueName, true, consumer)
                logger("Consuming messages from queue: $queueName")
            } catch (e: Exception) {
                logger("Error consuming messages from queue '$queueName': ${e.message ?: e.stackTraceToString()}")
            }
        }
    }

    /**
     * Closes the RabbitMQ connection and channel.
     */
    fun close() {
        try {
            // Close the channel and connection gracefully
            channel?.close()
            connection?.close()
            logger("Connection closed.")
        } catch (e: Exception) {
            logger("Error closing connection: ${e.message}")
        }
    }

    /**
     * Builder pattern for constructing a RabbitMQClient instance.
     * This allows for flexible configuration before instantiating the client.
     */
    class Builder {
        private var host = "localhost"
        private var port = 5672
        private var username = "guest"
        private var password = "guest"
        private var virtualHost = "/"
        private var automaticReconnect = false
        private var reconnectDelay: Long = 3000L
        private var logger: (String) -> Unit = { }

        fun host(host: String) = apply { this.host = host }
        fun port(port: Int) = apply { this.port = port }
        fun username(username: String) = apply { this.username = username }
        fun password(password: String) = apply { this.password = password }
        fun virtualHost(virtualHost: String) = apply { this.virtualHost = virtualHost }
        fun enableAutoReconnect() = apply { this.automaticReconnect = true }
        fun reconnectDelay(reconnectDelay: Long) = apply { this.reconnectDelay = reconnectDelay }
        fun logger(logger: (String) -> Unit) = apply { this.logger = logger }

        /**
         * Builds and returns an instance of RabbitMQClient.
         */
        suspend fun build() = RabbitMQClient(
            host,
            port,
            username,
            password,
            virtualHost,
            automaticReconnect,
            reconnectDelay,
            logger
        ).apply { init() }
    }
}
