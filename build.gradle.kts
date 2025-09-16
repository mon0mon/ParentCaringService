import org.gradle.kotlin.dsl.withType
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.epages.restdocs-api-spec") version "0.19.4"
    id("org.openapi.generator") version "7.11.0"
}

group = "com.lumanlab"
version = "0.0.1-SNAPSHOT"
description = "ParentCaringService"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")

    // RestDocs
    testImplementation("com.epages:restdocs-api-spec:0.19.4")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val snippetsDir = file("build/generated-snippets")

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(snippetsDir)
}

tasks.withType<BootJar> {
    dependsOn("copyApiDocs")
    from(file("${layout.buildDirectory.get()}/assets")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        into("BOOT-INF/classes")
    }
    archiveFileName.set("parent-caring-service-api.jar")
}

// OpenAPI3 스펙 생성을 위한 설정
openapi3 {
    setServer("http://localhost:8080/")
    title = "Parent Caring Service API Documentation"
    description = "Parent Caring Service API Documentation"
    version = "1.0.0"
    format = "yaml" // JSON도 가능
}

// 생성된 OpenAPI 3.0 문서를 build/assets/static/swagger 에 복사
tasks.register<Copy>("injectOpenApiSpecToSwagger") {
    dependsOn("openapi3")
    from("${layout.projectDirectory}/swagger-ui")
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))

    from(file("${layout.buildDirectory.get()}/api-spec/openapi3.yaml"))
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))
}

// 생성된 OpenAPI 3.0 문서를 build/assets/static/swagger 에 복사
tasks.register<Copy>("copyApiDocs") {
    dependsOn("injectOpenApiSpecToSwagger")
}
