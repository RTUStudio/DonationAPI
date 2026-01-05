dependencies {
    implementation(project(":Services:Common"))
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("org.xerial.snappy:snappy-java:1.1.10.8")
    implementation("io.socket:socket.io-client:2.1.2")

}

tasks.shadowJar {
    relocate("io.socket.client", "io.socket.v2.client")
    relocate("io.socket.parser", "io.socket.v2.parser")
    relocate("io.socket.engineio", "io.socket.v2.engineio")
}
