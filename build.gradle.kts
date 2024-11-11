plugins {
    kotlin("jvm") version "2.0.21"
    id("maven-publish")
}

group = "com.gtech.client"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

    val rabbitMQVersion = "5.22.0"
    api("com.rabbitmq:amqp-client:$rabbitMQVersion")

    val coroutineVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = version
        }
    }
}
