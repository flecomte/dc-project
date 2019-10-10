import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koinVersion: String by project
val postgresjson_version: String by project
val jackson_version: String by project
val cucumber_version: String by project

plugins {
    application
    kotlin("jvm") version "1.3.40"
}

group = "fr.dcproject"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.jetty.EngineMain"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jackson_version")
    implementation("net.pearx.kasechange:kasechange-jvm:1.1.0")
    implementation("com.auth0:java-jwt:3.8.2")
    implementation("com.github.jasync-sql:jasync-postgresql:1.0.7")
    implementation("fr.postgresjson:postgresjson:$postgresjson_version")
    implementation("com.sendgrid:sendgrid-java:4.4.1")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.0")
    testImplementation("org.amshove.kluent:kluent:1.4")
    testImplementation("io.cucumber:cucumber-java8:$cucumber_version")
    testImplementation("io.cucumber:cucumber-junit:$cucumber_version")
}
