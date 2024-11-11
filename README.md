# RabbitMQClient

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

### Declaring Exchanges
Create exchanges with various configurations as needed. This allows setting exchange types, durability, and other parameters.

```kotlin
client.declareExchange("myExchange", BuiltinExchangeType.DIRECT, durable = true, autoDelete = false)
```

### Declaring Queues with TTL
Declare queues with TTL (Time-To-Live) and expiration settings. This feature helps in managing message lifetimes effectively.

```kotlin
client.declareQueueWithTTL("myQueue", messageTTLInDays = 7, queueExpiresInYears = 1, durable = true, autoDelete = false)
```

### Sending Messages
Use `sendMessage` to send messages to an exchange with a routing key.

```kotlin
client.sendMessage("myExchange", "myRoutingKey", "Hello, RabbitMQ!")
```

### Consuming Messages
To consume messages from a queue, use the `consumeMessages` function and define a custom handler function to process each message.

```kotlin
client.consumeMessages("myQueue") { message ->
    println("Received message: $message")
}
```

### Closing the Client
Always close the client gracefully to ensure resources are properly released.

```kotlin
client.close()
```

## Design Choices and Methodology

### Builder Pattern
The `Builder` pattern provides flexibility and readability, allowing users to configure and instantiate `RabbitMQClient` with custom parameters while keeping the main API simple and clean.

### Connection Management and Auto-Reconnect
One of the core features is the robust connection management:
- **Automatic reconnection** handles initial connection failures and connection drops by retrying at specified intervals.
- **Reconnect delay** and maximum retry attempts provide control over how aggressively the client attempts to restore connections.

### Declarative APIs for Queues and Exchanges
The client provides convenient methods for declaring exchanges and queues with custom settings, including TTL (time-to-live) for messages and expiration for queues. This API makes configuring RabbitMQ resources straightforward and consistent.

### Messaging and Consumers
The send and consume methods use asynchronous processing, allowing for efficient and responsive message handling. The `consumeMessages` method is flexible and supports a custom handler for each message, providing users control over message processing logic.

### Logging Support
The client includes a custom logger option for easy integration with any logging system, giving users the freedom to capture and format log output as needed.

## Future Scope

Potential future enhancements for `RabbitMQClient` could include:
- **Enhanced error handling** and custom exception classes.
- **Monitoring and metrics** integration for tracking usage and health metrics.
- **Extended support** for advanced RabbitMQ features such as message priorities, dead-letter exchanges, and additional exchange types.
- **Asynchronous operations** for improved non-blocking behavior in high-throughput systems.
