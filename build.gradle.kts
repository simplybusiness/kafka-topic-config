import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*


tasks.withType<Test> {
	useJUnitPlatform()

	testLogging {
		events(PASSED, SKIPPED, FAILED, STANDARD_ERROR)
		exceptionFormat = FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true
		showStandardStreams = true
	}
}

plugins {
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.0"
	kotlin("plugin.spring") version "1.5.0"
	id("maven-publish")
	id("java-library")
	id("net.researchgate.release") version "2.6.0"
	id("com.adarshr.test-logger") version "2.1.1"
	id("maven-publish")
}

group = "com.simplybusiness"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8


repositories {
	mavenCentral()
}

dependencies {
	testCompileOnly("org.junit.jupiter", "junit-jupiter-api", "5.6.2")
	testImplementation("org.junit.jupiter","junit-jupiter-engine","5.6.2")
	testCompileOnly("org.junit.jupiter", "junit-jupiter-params", "5.6.2")
	implementation("org.apache.kafka:kafka-clients:2.4.0")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
	implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.3")


	implementation("org.apache.logging.log4j:log4j-core:2.14.1")
	implementation("org.apache.logging.log4j:log4j-api:2.14.1")
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
	implementation("commons-io:commons-io:2.8.0")
	testImplementation("com.salesforce.kafka.test:kafka-junit5:3.2.2")
	testImplementation("org.apache.kafka:kafka_2.12:2.4.0")
//    implementation("net.researchgate:gradle-release:2.6.0")
}

val jar: Jar by tasks

jar.enabled = true

java {

}

val afterReleaseBuild by tasks.existing
val beforeReleaseBuild by tasks.existing

tasks.withType<JavaCompile> {
	afterReleaseBuild {
		dependsOn("publish")
	}
}