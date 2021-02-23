import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.4.30"
	id("edu.sc.seis.launch4j") version "2.4.9"
}

group = "it.github.samuele794.twitch"
version = "1.0-SNAPSHOT"

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

launch4j {
	mainClassName = "botscript.MainBotKt"
	icon = "${projectDir}/icon/appIcon.ico"
	headerType= "console"
	dontWrapJar = true
	jar = "${projectDir}/build/libs/BotSo-${version}.jar"
}