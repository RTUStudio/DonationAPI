val apiVersion = project.property("plugin.api.version") as String
val frameworkVersion = project.property("framework.version") as String

repositories {
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
}

dependencies {
    implementation(project(":Platform:Common"))

    compileOnly(project(":Services:Common"))
    compileOnly(project(":Services:Chzzk"))
    compileOnly(project(":Services:SOOP"))
    compileOnly(project(":Services:SSAPI"))
    compileOnly(project(":Services:Cime"))
    compileOnly(project(":Services:Toonation"))
    compileOnly(project(":Services:Youtube"))

    // Plugin API
    val bukkitAPI = if (project.property("plugin.paper").toString().toBoolean()) {
        "io.papermc.paper:paper-api:${apiVersion}-R0.1-SNAPSHOT"
    } else {
        "org.spigotmc:spigot-api:${apiVersion}-R0.1-SNAPSHOT"
    }
    compileOnly(bukkitAPI)

    // RSFramework
    compileOnly("kr.rtustudio:framework-api:$frameworkVersion")

    // Kyori Adventure
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.16.0")

    // Dependency
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.retrooper:packetevents-spigot:2.10.1")
}

tasks.processResources {
    val props = mapOf(
        "version" to rootProject.version,
        "name" to rootProject.name,
        "main" to project.property("plugin.main"),
        "api_version" to apiVersion.substringBeforeLast("."),
        "author" to project.property("project.author")
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}