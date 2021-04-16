import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.typesafe.config.ConfigFactory
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.slf4j.LoggerFactory

val ktorVersion = "1.5.0"
val kotlinVersion = "1.4.30"
val coroutinesVersion = "1.4.3"
val logbackVersion = "1.2.3"
val koinVersion = "2.0.1"
val jacksonVersion = "2.12.1"

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
    `maven-publish`

    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"

    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("org.owasp.dependencycheck") version "6.1.5"
    id("org.sonarqube") version "3.1.1"
    id("net.nemerosa.versioning") version "2.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    id("com.avast.gradle.docker-compose") version "0.14.3"
    id("com.github.kt3k.coveralls") version "2.12.0"
}

application {
    mainClassName = "io.ktor.server.jetty.EngineMain"
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        classpath("com.typesafe:config:1.4.1")
        classpath("com.github.flecomte:postgres-json:2.1.2")
    }
}

tasks.distZip.configure { enabled = false }
tasks.distTar.configure { enabled = false }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

val migration by tasks.registering {
    group = "application"
    dependsOn(tasks.named("composeUp"))

    doLast {
        val config = ConfigFactory.parseFile(file("$buildDir/resources/main/application.conf")).resolve()
        val connection = Connection(
            host = config.getString("db.host"),
            port = config.getInt("db.port"),
            database = config.getString("db.database"),
            username = config.getString("db.username"),
            password = config.getString("db.password")
        )
        Migrations(
            connection,
            file("$buildDir/resources/main/sql/migrations").toURI(),
            file("$buildDir/resources/main/sql/functions").toURI()
        ).run {
            run()
        }
    }
}

val migrationTest by tasks.registering {
    group = "tests"
    dependsOn(tasks.named("testComposeUp"))
    finalizedBy(tasks.named("testComposeDown"))
    doLast {
        val config = ConfigFactory.parseFile(file("$buildDir/resources/test/application-test.conf")).resolve()
        val connection = Connection(
            host = config.getString("db.host"),
            port = config.getInt("db.port"),
            database = config.getString("db.database"),
            username = config.getString("db.username"),
            password = config.getString("db.password")
        )
        Migrations(
            connection,
            file("$buildDir/resources/main/sql/migrations").toURI(),
            file("$buildDir/resources/main/sql/functions").toURI()
        ).run {
            run()
            connection.disconnect()
        }
    }
}

val testSql by tasks.registering {
    group = "tests"
    dependsOn(tasks.named("processResources"))
    dependsOn(tasks.named("processTestResources"))

    doLast {
        val config = ConfigFactory.parseFile(file("$buildDir/resources/test/application-test.conf")).resolve()

        val connection = Connection(
            host = config.getString("db.host"),
            port = config.getInt("db.port"),
            database = config.getString("db.database"),
            username = config.getString("db.username"),
            password = config.getString("db.password")
        )

        Migrations(
            connection,
            file("$buildDir/resources/main/sql/migrations").toURI(),
            file("$buildDir/resources/main/sql/functions").toURI(),
            file("$buildDir/resources/test/sql/fixtures").toURI()
        ).run()

        Requester.RequesterFactory(
            connection = connection,
            queriesDirectory = file("$buildDir/resources/test/sql").toURI()
        ).createRequester().run {
            getQueries().map {
                try {
                    it.sendQuery() == 0
                } catch (e: Exception) {
                    false
                }
            }
        }

        connection.disconnect()
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles("META-INF/services")
    archiveFileName.set("${archiveBaseName.get()}-latest-all.${archiveExtension.get()}")
}

tasks.sonarqube.configure {
    dependsOn(tasks.jacocoTestReport)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

tasks.test {
    useJUnit()
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

coveralls {
    sourceDirs.add("src/main/kotlin")
}

apply(plugin = "docker-compose")
dockerCompose {
    projectName = "dc-project"
    useComposeFiles = listOf("docker-compose.yml")
    startedServices = listOf("db", "elasticsearch", "rabbitmq", "redis")
    stopContainers = false
    removeVolumes = false
    removeContainers = false
    isRequiredBy(project.tasks.run)

    createNested("testSql").apply {
        projectName = "dc-project_test"
        useComposeFiles = listOf("docker-compose-test.yml")
        startedServices = listOf("db", "elasticsearch")
        stopContainers = false
    }

    createNested("test").apply {
        projectName = "dc-project_test"
        useComposeFiles = listOf("docker-compose-test.yml")
        stopContainers = false
    }
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
    ignoreFailures = true
//    config = files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
        sarif.enabled = true // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}

tasks.withType<Detekt> {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = "11"
    ignoreFailures = true
}

val setMaxMapCount = tasks.create<Exec>("setMaxMapCount") {
    group = "docker"
    doFirst {
        if (OperatingSystem.current().isWindows) {
            commandLine("cmd", "/c", "Powershell -ExecutionPolicy Bypass; wsl -d docker-desktop sysctl -w vm.max_map_count=262144")
        } else if (OperatingSystem.current().isLinux) {
            commandLine("sysctl -w vm.max_map_count=262144")
        }
    }
}

tasks.named("testComposeUp").configure {
    if (OperatingSystem.current().isWindows) {
        dependsOn(setMaxMapCount)
    }
}

tasks.register("testWithDependencies", Test::class) {
    group = "tests"
    dependsOn(tasks.named("testComposeUp"))
    dependsOn(tasks.ktlintCheck)
    dependsOn(testSql)
    finalizedBy(tasks.sonarqube) // report is always generated after tests run
}
tasks.register("testArticles", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("article")
    }
}
tasks.register("testCitizens", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("citizen")
    }
}
tasks.register("testComments", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("comment")
    }
}
tasks.register("testConstitutions", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("constitution")
    }
}
tasks.register("testFollows", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("follow")
    }
}
tasks.register("testNotifications", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("notification")
    }
}
tasks.register("testOpinions", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("opinion")
    }
}
tasks.register("testVotes", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("vote")
    }
}
tasks.register("testWorkgroups", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("workgroup")
    }
}
tasks.register("testViews", Test::class) {
    group = "tests"
    useJUnitPlatform {
        includeTags("view")
    }
}

dependencyCheck {
    formats = listOf(ReportGenerator.Format.HTML, ReportGenerator.Format.XML)
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://kotlin.bintray.com/ktor")
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/konform-kt/konform")
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-client-jetty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jacksonVersion")
    implementation("net.pearx.kasechange:kasechange-jvm:1.3.0")
    implementation("com.auth0:java-jwt:3.12.0")
    implementation("com.github.jasync-sql:jasync-postgresql:1.1.6")
    implementation("com.github.flecomte:postgres-json:2.1.2")
    implementation("com.sendgrid:sendgrid-java:4.7.1")
    implementation("io.lettuce:lettuce-core:5.3.6.RELEASE") // TODO update to 6.0.2
    implementation("com.rabbitmq:amqp-client:5.10.0")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:6.7.1")
    implementation("com.jayway.jsonpath:json-path:2.5.0")
    implementation("com.avast.gradle:gradle-docker-compose-plugin:0.14.0")
    implementation("io.konform:konform-jvm:0.2.0")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.amshove.kluent:kluent:1.61")
    testImplementation("io.mockk:mockk-agent-api:1.10.6")
    testImplementation("io.mockk:mockk-agent-jvm:1.10.6")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.thedeanda:lorem:2.1")
    testImplementation("org.openapi4j:openapi-operation-validator:1.0.6")
    testImplementation("org.openapi4j:openapi-parser:1.0.6")
}
