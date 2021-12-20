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
	id("signing")
	id("java")
}

group = "com.simplybusiness"
version = "1.0.11-SNAPSHOT"
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


	implementation("org.apache.logging.log4j:log4j-core:2.17.0")
	implementation("org.apache.logging.log4j:log4j-api:2.17.0")
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
	implementation("commons-io:commons-io:2.8.0")
	testImplementation("com.salesforce.kafka.test:kafka-junit5:3.2.2")
	testImplementation("org.apache.kafka:kafka_2.12:2.4.0")
//    implementation("net.researchgate:gradle-release:2.6.0")
}

val jar: Jar by tasks

jar.enabled = true

java {
	withSourcesJar()
	withJavadocJar()
}

val afterReleaseBuild by tasks.existing
val beforeReleaseBuild by tasks.existing

tasks.withType<JavaCompile> {
	afterReleaseBuild {
		dependsOn("publish")
	}
}


signing {
	sign(configurations.archives.get())
}

publishing {
	publications {
		create<MavenPublication>("kafka-topic-config") {
			from(components["java"])

//			artifact(tasks["sourcesJar"])
//			artifact(tasks["javadocJar"])

			repositories {
				maven {
					credentials {
//						username = project.property("ossrhUsername").toString()
//						password = project.property("ossrhPassword").toString()
						username = System.getenv("OSSRHUSERNAME")
						password = System.getenv("OSSRPASSWORD")
					}

					//s01.oss.sonatype.org
					val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
					val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
					url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
				}
			}

			pom {
				name.set("kafka-topic-config")
				description.set("support for yaml based Kafka topic configuration")
				url.set("https://github.com/simplybusiness/kafka-topic-config")

				licenses {
					license {
						name.set("MIT License")
						url.set("https://github.com/simplybusiness/kafka-topic-config#-license")
					}
				}

				developers {
					developer {
						id.set("jameswalkerdine")
						name.set("JamesWalkerdine")
						email.set("kafkatopicconfig@simplybusiness.co.uk")
						url.set("https://github.com/jameswalkerdine")
						roles.addAll("developer")
						timezone.set("Europe/London")
					}
				}

				scm {
					connection.set("scm:git:https://github.com/simplybusiness/kafka-topic-config.git")
					developerConnection.set("scm:git:ssh://github.com:jameswalkerdine/kafka-topic-config.git")
					url.set("https://github.com/simplybusiness/kafka-topic-config")
				}
			}
		}
	}
}

extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

signing {
	setRequired({
		(project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
	})
	sign(publishing.publications["kafka-topic-config"])
}

