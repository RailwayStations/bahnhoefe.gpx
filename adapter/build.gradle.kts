plugins {
    alias(libs.plugins.kotlin.spring)
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    implementation(project("::openapi"))
    implementation(project("::db-migration"))
    implementation(project("::core"))
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation(libs.swagger.annotations)
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(libs.spring.security.oauth2.authorization.server)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation(libs.jooq.core)
    implementation(libs.lazysodium.java)
    implementation(libs.jna)
    implementation("commons-codec:commons-codec")
    implementation(libs.commons.io)
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.bootstrap)

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.webjars:webjars-locator-lite")

    testImplementation(testFixtures(project("::core")))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.awaitility:awaitility")
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
    testFixturesImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation(libs.swagger.request.validator.spring.webmvc)
    testImplementation(libs.swagger.request.validator.mockmvc)
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.xmlunit:xmlunit-assertj3")
    testImplementation(libs.json.unit.spring)
    testImplementation(libs.wiremock.jre8.standalone)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.greenmail)

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
