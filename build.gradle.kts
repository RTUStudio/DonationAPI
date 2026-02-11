plugins {
    java
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val project_version: String by project
val project_group: String by project
val minecraft_version: String by project
val rsf_version: String by project

version = project_version
group = project_group

tasks.runServer {
    minecraftVersion(minecraft_version)
    downloadPlugins {
        url("https://ci.codemc.io/job/RTUStudio/job/RSFramework/lastSuccessfulBuild/artifact/builds/plugin/RSFramework-${rsf_version}.jar")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
    }

    dependencies {
        // Google/Apache
        compileOnly("com.google.code.gson:gson:2.13.1")
        compileOnly("com.google.guava:guava:33.4.8-jre")
        compileOnly("org.apache.commons:commons-lang3:3.18.0")
        compileOnly("it.unimi.dsi:fastutil:8.5.18")

        // Lombok
        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")
        compileOnly("org.slf4j:slf4j-api:2.0.16")
        compileOnly("org.jetbrains:annotations:24.1.0")
    }

    tasks.jar {
        finalizedBy("shadowJar")
    }
}

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
        name = "Sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    // RSFramework
    maven("https://repo.codemc.io/repository/rtustudio/")

    // PlaceholderAPI / PacketEvents
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    // Platform:Bukkit에 모든 것이 포함됨
    implementation(project(path = ":Platform:Bukkit", configuration = "shadow"))

    implementation(project(path = ":Services:ChzzkOfficial", configuration = "shadow"))
    implementation(project(path = ":Services:SSAPI", configuration = "shadow"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set(rootProject.name)
    doLast {
        copy {
            from(archiveFile.get().asFile)
            into(file("$rootDir/builds"))
        }
    }
}