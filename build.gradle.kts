import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.4.30"
}

group = "it.github.samuele794.twitch"
version = "1.0"

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	testImplementation(kotlin("test-junit"))
	implementation(group = "com.github.twitch4j", name = "twitch4j", version = "1.2.0")
	implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.0")
	implementation("de.vandermeer:asciitable:0.3.2")
}

tasks.register<Jar>("uberJar") {
	manifest {
		attributes(
			"Main-Class" to "botscript.MainBotKt",
			"Implementation-Title" to "Gradle",
			"Implementation-Version" to archiveVersion
		)
	}
	from(sourceSets.main.get().output)

	exclude {
		it.name == "config.json"
	}

	dependsOn(configurations.runtimeClasspath)

	from({
		configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
	})
}

tasks.test {
	useJUnit()
}

tasks.withType<KotlinCompile>() {
	kotlinOptions.jvmTarget = "1.8"
}