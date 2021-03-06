import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("jvm") version "1.3.71"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

allprojects {
    group = "org.botlaxy"
    version = "0.0.21"

    repositories {
        jcenter()
        mavenCentral()
    }
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val jacksonVersion = "2.10.3"
val logbackVersion = "1.2.3"
val kotlinLogVersion = "1.7.7"
val mapDb = "3.0.8"
val jnaVersion = "4.2.2"
val emojiVersion = "5.1.1"
val okHttpVersion = "4.4.1"
val ktorVersion = "1.3.2"
val mockWebServerVersion = "4.4.0"

subprojects {

    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.bintray")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect", kotlinVersion))
        implementation(kotlin("script-util", kotlinVersion))
        implementation(kotlin("script-runtime", kotlinVersion))
        implementation(kotlin("compiler-embeddable", kotlinVersion))
        implementation(kotlin("scripting-compiler-embeddable", kotlinVersion))
        implementation("io.ktor:ktor-server-netty:$ktorVersion")
        implementation("io.ktor:ktor-jackson:$ktorVersion")
        implementation("com.vdurmont:emoji-java:$emojiVersion")
        implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
        implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("net.java.dev.jna:jna:$jnaVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        implementation("io.github.microutils:kotlin-logging:$kotlinLogVersion")
        implementation("org.mapdb:mapdb:${mapDb}")
    }

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    publishing {
        publications {
            create<MavenPublication>("telegramit") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
                from(components["java"])

                artifact(sourcesJar)

                pom {
                    name.set("Telegramit")
                    description.set("Telegram chat bot framework")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("vitaxa")
                            name.set("Vitaliy Banin")
                            email.set("vitaxa93gamebox@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/vitaxa/telegramit")
                        developerConnection.set("scm:git:ssh://github.com/vitaxa/telegramit")
                    }
                }
            }
        }
    }

    bintray {
        user = project.findProperty("bintrayUser") as String?
        key = project.findProperty("bintrayApiKey") as String?
        publish = true

        setPublications("telegramit")

        pkg.apply {
            repo = "telegramit"
            name = project.name
            githubRepo = "https://github.com/vitaxa/telegramit"
            vcsUrl = "https://github.com/vitaxa/telegramit"
            description = "Telegram chat bot framework"
            setLabels("kotlin", "telegram", "chat", "bot", "dsl")
            setLicenses("Apache-2.0")
            desc = description
            version.apply {
                name = project.version.toString()
            }
        }
    }

}
