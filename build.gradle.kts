plugins {
    kotlin("jvm") version "2.0.21"
}

group = "com.gtech.client"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    val rabbitMQVersion = "5.22.0"
    implementation("com.rabbitmq:amqp-client:$rabbitMQVersion")

    val coroutineVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

}

tasks.test {
    useJUnitPlatform()
}