plugins {
    java
    `maven-publish`
    id("io.freefair.lombok") version "9.2.0"
    id("com.gradleup.shadow") version "9.3.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val apiVersion = project.property("plugin.api.version") as String
val frameworkVersion = project.property("framework.version") as String

version = project.property("project.version") as String
group = project.property("project.group") as String

tasks.runServer {
    minecraftVersion(project.property("minecraft.version") as String)
    downloadPlugins {
        url("https://ci.codemc.io/job/RTUStudio/job/RSFramework/lastSuccessfulBuild/artifact/builds/plugin/RSFramework-$frameworkVersion.jar")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.gradleup.shadow")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(project.property("java.version") as String))
    }

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
        compileOnly("org.projectlombok:lombok:1.18.42")
        annotationProcessor("org.projectlombok:lombok:1.18.42")
        compileOnly("org.slf4j:slf4j-api:2.0.16")
        compileOnly("org.jetbrains:annotations:24.1.0")

        // Network & Testing
        compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
        testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
        testImplementation("com.google.code.gson:gson:2.13.1")
        testImplementation("org.slf4j:slf4j-simple:2.0.16")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.jar {
        finalizedBy("shadowJar")
    }
}

repositories {
    maven {
        name = "Sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    // Platform:Bukkit에 모든 것이 포함됨
    implementation(project(path = ":Platform:Bukkit", configuration = "shadow"))

    implementation(project(path = ":Services:Chzzk", configuration = "shadow"))
    implementation(project(path = ":Services:SOOP", configuration = "shadow"))
    implementation(project(path = ":Services:SSAPI", configuration = "shadow"))
    implementation(project(path = ":Services:Cime", configuration = "shadow"))
    implementation(project(path = ":Services:Toonation", configuration = "shadow"))
    implementation(project(path = ":Services:Youtube", configuration = "shadow"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set(project.name)
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/rtustudio/")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("plugin") {
            groupId = "kr.rtustudio"
            artifactId = "donationapi"
            version = project.version.toString()

            from(components["shadow"])
        }
    }
}