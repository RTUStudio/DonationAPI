dependencies {
    implementation(project(":Services:Common"))
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("io.undertow:undertow-core:2.3.20.Final")
    implementation("org.jboss.xnio:xnio-api:3.8.14.Final")
    implementation("org.jboss.xnio:xnio-nio:3.8.14.Final")
    implementation("io.socket:socket.io-client:1.0.1")
}

tasks.shadowJar {
    relocate("io.socket.client", "io.socket.v1.client")
    relocate("io.socket.parser", "io.socket.v1.parser")
    relocate("io.socket.engineio", "io.socket.v1.engineio")
}
