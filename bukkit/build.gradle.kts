plugins {
    id("com.gradleup.shadow")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.19.4-R0.1-SNAPSHOT")
    compileOnly(group = "com.github.Puremin0rez", name = "WorldBorder", version = "1.19") {
        isTransitive = false
    }
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.2")
    implementation(project(":Chunky-common"))
    implementation(project(":Chunky-paper"))
    implementation(project(":Chunky-folia"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to project.property("artifactName"),
                "version" to project.version,
                "group" to project.group,
                "author" to project.property("author"),
                "description" to project.property("description"),
            )
        }
    }
    shadowJar {
        minimize {
            exclude(project(":Chunky-common"))
            exclude(project(":Chunky-paper"))
            exclude(project(":Chunky-folia"))
        }
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
    }
}
