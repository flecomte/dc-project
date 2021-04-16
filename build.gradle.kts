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

val ktorVersion = "+"
val kotlinVersion = "1.4.+"
val coroutinesVersion = "+"

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

    kotlin("jvm") version "1.4.+"
    kotlin("plugin.serialization") version "1.4.+"

    id("com.github.johnrengelman.shadow") version "+"
    id("org.jlleitschuh.gradle.ktlint") version "+"
    id("org.owasp.dependencycheck") version "+"
    id("org.sonarqube") version "+"
    id("net.nemerosa.versioning") version "+"
    id("io.gitlab.arturbosch.detekt") version "+"
    id("com.avast.gradle.docker-compose") version "+"
    id("com.github.kt3k.coveralls") version "+"
}

dependencyLocking {
    lockAllConfigurations()
    // lockMode.set(LockMode.STRICT)
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
        classpath("com.typesafe:config:+")
        classpath("com.github.flecomte:postgres-json:+")
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
    dependsOn(tasks.jacocoTestReport)
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:+")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-client-jetty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:+")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.insert-koin:koin-ktor:+")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:+")
    implementation("net.pearx.kasechange:kasechange-jvm:+")
    implementation("com.auth0:java-jwt:+")
    implementation("com.github.jasync-sql:jasync-postgresql:+")
    implementation("com.github.flecomte:postgres-json:+")
    implementation("com.sendgrid:sendgrid-java:+")
    implementation("io.lettuce:lettuce-core:5.3.6.RELEASE") // TODO update to 6.0.2
    implementation("com.rabbitmq:amqp-client:+")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:6+")
    implementation("com.jayway.jsonpath:json-path:+")
    implementation("com.avast.gradle:gradle-docker-compose-plugin:+")
    implementation("io.konform:konform-jvm:+")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.insert-koin:koin-test:+")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:+")
    testImplementation("io.mockk:mockk:+")
    testImplementation("org.junit.jupiter:junit-jupiter:+")
    testImplementation("org.amshove.kluent:kluent:+")
    testImplementation("io.mockk:mockk-agent-api:+")
    testImplementation("io.mockk:mockk-agent-jvm:+")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.thedeanda:lorem:+")
    testImplementation("org.openapi4j:openapi-operation-validator:+")
    testImplementation("org.openapi4j:openapi-parser:+")
}
