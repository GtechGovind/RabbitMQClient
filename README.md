# RabbitMQClient

[![](https://jitpack.io/v/GtechGovind/RabbitMQClient.svg)](https://jitpack.io/#GtechGovind/RabbitMQClient)

A Kotlin-based, simplified client for interacting with RabbitMQ. The `RabbitMQClient` library enhances the standard RabbitMQ client by adding automatic reconnection, easier connection management, queue management with TTL, and an intuitive API for sending and receiving messages.

## Why Build a Custom Client Library?

### Key Reasons for Customization
The official RabbitMQ client library is robust and highly configurable, but can be complex for developers who need a quick and simple way to manage connections and message flows. Our custom `RabbitMQClient` simplifies RabbitMQ interactions by:
- **Streamlining connection management** with configurable retry and reconnection mechanisms.
- **Providing automatic reconnection**, handling both initial connection attempts and any disruptions that might occur.
- **Offering intuitive API methods** for common tasks like declaring exchanges, creating queues with TTL, and sending/receiving messages.

### Why Implement Auto-Reconnect?
While RabbitMQ’s default library supports automatic reconnection, it only activates after an initial connection is established. Our custom client library improves reliability by:
- Retrying connection multiple times on initial failures.
- Managing persistent reconnections seamlessly when connections drop unexpectedly.
- Minimizing message flow disruption by maintaining consistent RabbitMQ access.

## Installation

To add the `RabbitMQClient` library to your project, include the dependency using JitPack.

### Step 1: Add JitPack to your Project
In your root `build.gradle` or `settings.gradle.kts`, add JitPack to your repositories:

#### Groovy
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### Kotlin
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### Step 2: Add the Dependency
In your module `build.gradle` or `build.gradle.kts`, add the following dependency:

#### Groovy
```groovy
dependencies {
    implementation 'com.github.GtechGovind:RabbitMQClient:Tag'
}
```

#### Kotlin
```kotlin
dependencies {
    implementation("com.github.GtechGovind:RabbitMQClient:Tag")
}
```

Replace `Tag` with the specific release version you want to use.

## Usage

### Setting Up the Client
Configure the client using the provided `Builder` for flexibility.

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

## Producers

In RabbitMQ, a **Producer** is responsible for sending messages to an exchange. This library simplifies the process by allowing you to declare exchanges and queues, and to send messages easily.

### Declaring Exchanges
An exchange routes messages to the correct queues based on the routing keys. Our `RabbitMQClient` supports different exchange types, including `DIRECT`, `FANOUT`, `TOPIC`, and `HEADERS`.

```kotlin
client.declareExchange("myExchange", BuiltinExchangeType.DIRECT, durable = true, autoDelete = false)
```

#### Exchange Parameters
- `name`: The exchange name.
- `type`: The type of exchange (`DIRECT`, `FANOUT`, `TOPIC`, etc.).
- `durable`: If `true`, the exchange will survive a broker restart.
- `autoDelete`: If `true`, the exchange will be deleted when no longer in use.

### Declaring Queues with TTL
Queues store messages until they are consumed. With `RabbitMQClient`, you can set Time-To-Live (TTL) for queues to automatically expire old messages, which is useful for managing memory and ensuring only recent messages are processed.

```kotlin
client.declareQueueWithTTL("myQueue", messageTTLInDays = 7, queueExpiresInYears = 1, durable = true, autoDelete = false)
```

#### Queue Parameters
- `name`: The queue name.
- `messageTTLInDays`: Sets how long messages live in the queue.
- `queueExpiresInYears`: Sets how long the queue itself remains available.
- `durable`: If `true`, the queue will survive a broker restart.
- `autoDelete`: If `true`, the queue will be deleted when no longer in use.

### Sending Messages
With `RabbitMQClient`, sending messages to an exchange is simplified. You only need to specify the exchange, the routing key, and the message content.

```kotlin
client.sendMessage("myExchange", "myRoutingKey", "Hello, RabbitMQ!")
```

#### Message Sending Parameters
- `exchange`: The exchange to which the message will be sent.
- `routingKey`: The routing key used by the exchange to route the message to the appropriate queue.
- `message`: The message content.

## Consumers

A **Consumer** is responsible for receiving messages from a queue. The `RabbitMQClient` makes it easy to set up consumers and process incoming messages with a custom handler function.

### Consuming Messages
To start consuming messages, simply specify the queue name and a handler function to process each message.

```kotlin
client.consumeMessages("myQueue") { message ->
    println("Received message: $message")
}
```

#### Consumer Parameters
- `queue`: The queue from which messages will be consumed.
- `handler`: A lambda function to process each message as it arrives.

### Handling Long-Running Tasks
Consumers may need to perform long-running tasks when processing messages. It’s recommended to handle these tasks asynchronously to avoid blocking the consumer, which can lead to message backlog.

### Auto Acknowledgments
By default, `RabbitMQClient` uses auto-acknowledgments, meaning the broker assumes that the message was successfully processed as soon as it is delivered to the consumer. This is convenient, but if your use case requires manual acknowledgment, you can adjust the library to handle this for more control.

### Error Handling in Consumers
To ensure reliable message processing, the library supports error handling during message consumption. If an error occurs, you can configure the client to retry processing the message, log the error, or move it to a dead-letter queue for later inspection.

---

### Closing the Client

Always close the client connection gracefully when done:

```kotlin
client.close()
```

## Future Scope

Here are some potential enhancements and additions that could be considered for future development:
- **Enhanced Error Handling**: Adding more robust error handling mechanisms and retries.
- **Metrics and Monitoring**: Integrating with monitoring tools to track the performance and health of the RabbitMQ interactions.
- **Advanced Features**: Implementing more advanced RabbitMQ features like priorities, dead-letter exchanges, and more.
- **Configuration Management**: Simplifying configuration management using external configuration files or environment variables.
- **Asynchronous Operations**: Further optimizing the client for non-blocking asynchronous operations.
