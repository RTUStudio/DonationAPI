dependencies {
    implementation(project(":Services:Common"))
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("io.undertow:undertow-core:2.3.20.Final")
    implementation("org.jboss.xnio:xnio-api:3.8.14.Final")
    implementation("org.jboss.xnio:xnio-nio:3.8.14.Final")
}