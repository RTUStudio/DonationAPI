val project_author: String by project
val plugin_main: String by project
val rsf_version: String by project
val api_version: String by project
val paper_plugin: String by project


repositories {
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.codemc.io/repository/rtustudio/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    implementation(project(":Platform:Common"))

    compileOnly(project(":Services:Common"))
    compileOnly(project(":Services:ChzzkOfficial"))
    compileOnly(project(":Services:SSAPI"))
    
    // Plugin API
    val plugin_api = if (paper_plugin.toBoolean()) {
        "io.papermc.paper:paper-api:${api_version}-R0.1-SNAPSHOT"
    } else {
        "org.spigotmc:spigot-api:${api_version}-R0.1-SNAPSHOT"
    }
    compileOnly(plugin_api)
    
    // RSFramework
    compileOnly("kr.rtustudio:framework-api:${rsf_version}")
    
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
        "main" to plugin_main,
        "api_version" to api_version.substringBeforeLast("."),
        "author" to project_author
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}