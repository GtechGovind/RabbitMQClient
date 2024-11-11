# RabbitMQClient

## Why Build a Custom Client Library?

The primary reason for building our own client library on top of the RabbitMQ client was to provide enhanced functionality and ease of use. The standard RabbitMQ client library, while powerful, can be complex and requires extensive setup and error handling. By creating a custom client library, we aimed to:
- Simplify connection management and message handling.
- Provide automatic reconnection capabilities.
- Offer a more intuitive API for declaring exchanges and queues, and sending/receiving messages.
- Include additional features such as TTL and expiration settings for queues.

### Why Implement Auto-Reconnect?

RabbitMQ provides automatic reconnection, but it only triggers after an initial successful connection. If the initial connection attempt fails, RabbitMQ does not retry. To address this, our client library implements an auto-reconnect feature that:
- Attempts to reconnect multiple times if the initial connection fails.
- Handles connection loss scenarios and tries to re-establish the connection automatically.
- Ensures minimal disruption in message flow by maintaining persistent connections.

## How to Use RabbitMQClient

### Setting Up the Client

To start using the RabbitMQClient, configure and instantiate it using the Builder pattern provided.

```kotlin
val client = RabbitMQClient.Builder()
    .host("your_host")
    .port(5672)
    .username("your_username")
    .password("your_password")
    .virtualHost("/")
    .enableAutoReconnect()
    .reconnectDelay(3000L)
    .logger { message -> println(message) }
    .build()
```

### Producers

#### Declaring Exchanges

You can declare exchanges with various configurations.

```kotlin
client.declareExchange("myExchange", BuiltinExchangeType.DIRECT, durable = true, autoDelete = false)
```

#### Declaring Queues

You can declare queues with Time-To-Live (TTL) and expiration settings.

```kotlin
client.declareQueueWithTTL("myQueue", messageTTLInDays = 7, queueExpiresInYears = 1, durable = true, autoDelete = false)
```

#### Sending Messages

To send a message to an exchange with a specific routing key:

```kotlin
client.sendMessage("myExchange", "myRoutingKey", "Hello, RabbitMQ!")
```

### Consumers

#### Consuming Messages

To start consuming messages from a queue and handle them with a custom handler function:

```kotlin
client.consumeMessages("myQueue") { message ->
    println("Received message: $message")
}
```

### Closing the Client

Always close the client connection gracefully when done:

```kotlin
client.close()
```

## Future Scope

Here are some potential enhancements and additions that could be considered for future development:
- **Enhanced Error Handling:** Adding more robust error handling mechanisms and retries.
- **Metrics and Monitoring:** Integrating with monitoring tools to track the performance and health of the RabbitMQ interactions.
- **Advanced Features:** Implementing more advanced RabbitMQ features like priorities, dead-letter exchanges, and more.
- **Configuration Management:** Simplifying configuration management using external configuration files or environment variables.
- **Asynchronous Operations:** Further optimizing the client for non-blocking asynchronous operations.
