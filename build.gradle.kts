import java.io.ByteArrayOutputStream
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar

plugins {
    eclipse
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.9" apply false
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.gradleup.shadow")

    group = project.property("group") as String
    version = "${project.property("version")}.${commitsSinceLastTag()}"

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 17
            options.compilerArgs.add("-Xlint:none")
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }

        named<Jar>("jar") {
            archiveClassifier.set("noshade")
        }

        named<ShadowJar>("shadowJar") {
            archiveClassifier.set("")
            archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
            isEnableRelocation = true
            relocationPrefix = "${project.group}.${rootProject.name.lowercase()}.shadow"
        }

        named("build") {
            dependsOn("shadowJar")
        }
    }

    publishing {
        repositories {
            if (hasProperty("mavenUsername") && hasProperty("mavenPassword")) {
                maven {
                    credentials {
                        username = project.property("mavenUsername") as String
                        password = project.property("mavenPassword") as String
                    }
                    url = uri("https://repo.codemc.io/repository/maven-releases/")
                }
            }
        }
        publications.create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

val pluginArtifact by tasks.registering(Sync::class) {
    dependsOn(":Chunky-bukkit:shadowJar")
    from(project(":Chunky-bukkit").tasks.named("shadowJar"))
    into(layout.buildDirectory.dir("libs"))
}

tasks.named("build") {
    dependsOn(pluginArtifact)
}

fun commitsSinceLastTag(): String {
    val tagDescription = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = tagDescription
    }
    val desc = tagDescription.toString().trim()
    return if ('-' !in desc) "0" else desc.split('-')[1]
}
