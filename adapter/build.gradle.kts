plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.kapt)
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    implementation(project("::openapi"))
    implementation(project("::core"))
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.swagger.annotations)
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(libs.spring.security.oauth2.authorization.server)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation(libs.jdbi3.spring5)
    implementation(libs.jdbi3.kotlin)
    implementation(libs.jdbi3.kotlin.sqlobject)
    implementation(libs.lazysodium.java)
    implementation(libs.jna)
    implementation("commons-codec:commons-codec")
    implementation(libs.commons.io)
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.bootstrap)

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("org.webjars:webjars-locator-core")

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
    testFixturesImplementation("org.testcontainers:mariadb")
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
