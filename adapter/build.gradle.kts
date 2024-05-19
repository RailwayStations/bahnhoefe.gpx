plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.kapt)
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    implementation(project("::openapi"))
    implementation(project("::core"))
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.security.oauth2.authorization.server)
    implementation(libs.liquibase.core)
    implementation(libs.jdbi3.spring5)
    implementation(libs.jdbi3.kotlin)
    implementation(libs.jdbi3.kotlin.sqlobject)
    implementation(libs.lazysodium.java)
    implementation(libs.jna)
    implementation(libs.commons.codec)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.bootstrap)

    kapt(libs.spring.boot.configuration.processor)

    runtimeOnly(libs.mariadb.java.client)
    runtimeOnly(libs.webjars.locator)

    testImplementation(testFixtures(project("::core")))
    testFixturesImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(libs.spring.security.test)
    testImplementation("org.awaitility:awaitility")
    testFixturesImplementation(libs.testcontainers)
    testFixturesImplementation(libs.testcontainers.junit.jupiter)
    testFixturesImplementation(libs.testcontainers.mariadb)
    testImplementation(libs.swagger.request.validator.core)
    testImplementation(libs.swagger.request.validator.spring.webmvc)
    testImplementation(libs.swagger.request.validator.mockmvc)
    testImplementation("org.assertj:assertj-core")
    testImplementation(libs.xmlunit.assertj3)
    testImplementation(libs.json.unit.spring)
    testImplementation(libs.wiremock.jre8.standalone)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.greenmail)

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
