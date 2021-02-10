import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.slf4j.LoggerFactory

val ktor_version: String by project
val kotlin_version: String by project
val coroutinesVersion: String by project
val logback_version: String by project
val koinVersion: String by project
val jackson_version: String by project
val cucumber_version: String by project

group = "com.github.flecomte"
version = versioning.info.run {
    if (dirty) {
        versioning.info.full
    } else {
        versioning.info.lastTag
    }
}

plugins {
    jacoco
    application
    maven

    id("maven-publish")
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"

    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("org.owasp.dependencycheck") version "6.0.5"
    id("org.sonarqube") version "3.1.1"
    id("net.nemerosa.versioning") version "2.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0-RC1"
}

application {
    mainClassName = "io.ktor.server.jetty.EngineMain"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles("META-INF/services")
        archiveFileName.set("${archiveBaseName.get()}-latest-all.${archiveExtension.get()}")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}
tasks.test {
    useJUnit()
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
//    maxHeapSize = "1G"
}

publishing {
    if (versioning.info.dirty == false) {
        repositories {
            maven {
                name = "dc-project"
                group = "com.github.flecomte"
                url = uri("https://maven.pkg.github.com/flecomte/dc-project")
                credentials {
                    username = System.getenv("GITHUB_USERNAME")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>("dc-project") {
                from(components["java"])
                artifact(sourcesJar)
            }
        }
    } else {
        LoggerFactory.getLogger("gradle")
            .warn("The git is DIRTY (${versioning.info.full})")
    }
}

jacoco {
    toolVersion = "0.8.6"
    applyTo(tasks.run.get())
}
tasks.register<JacocoReport>("applicationCodeCoverageReport") {
    executionData(tasks.run.get())
    sourceSets(sourceSets.main.get())
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
//    config = files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
        sarif.enabled = true // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}

tasks {
    withType<Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = "11"
    }
}

dependencyCheck {
    formats = listOf(ReportGenerator.Format.HTML, ReportGenerator.Format.XML)
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("io.ktor:ktor-client-jetty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jackson_version")
    implementation("net.pearx.kasechange:kasechange-jvm:1.3.0")
    implementation("com.auth0:java-jwt:3.12.0")
    implementation("com.github.jasync-sql:jasync-postgresql:1.1.6")
    implementation("com.github.flecomte:postgres-json:2.0.0")
    implementation("com.sendgrid:sendgrid-java:4.7.1")
    implementation("io.lettuce:lettuce-core:5.3.6.RELEASE") // TODO update to 6.0.2
    implementation("com.rabbitmq:amqp-client:5.10.0")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:6.7.1")
    implementation("com.jayway.jsonpath:json-path:2.5.0")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.mockk:mockk:1.10.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.amshove.kluent:kluent:1.61")
    testImplementation("io.cucumber:cucumber-java8:$cucumber_version")
    testImplementation("io.cucumber:cucumber-junit:$cucumber_version")
}
