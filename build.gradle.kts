import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    maven {
      val nexusRepo: String by project
      url = uri(nexusRepo)
    }
  }
}

plugins {
  application
  kotlin("jvm") version "1.4.10"
  kotlin("plugin.spring") version "1.4.10"

  id("idea")
}

application {
  mainClassName = "sample.AppKt"
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

repositories {
  maven {
    val nexusRepo: String by project
    url = uri(nexusRepo)
  }
}

configurations {
  all {
    exclude(group = "com.vaadin.external.google", module = "android-json")
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(group = "org.springframework.cloud", module = "spring-cloud-contract-shade")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-reactor-jetty")

    // TODO jetty must be removed together with wiremock & stub runner
    resolutionStrategy {
      eachDependency {
        if(requested.group == "org.eclipse.jetty") useVersion("9.4.31.v20200723")
        if(requested.group == "org.jetbrains.exposed") useVersion("0.24.1")
      }
    }
  }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.4.0")

  implementation("org.springframework.boot:spring-boot-starter-actuator:2.3.5.RELEASE")
  implementation("org.springframework.boot:spring-boot-starter-webflux:2.3.5.RELEASE")
  implementation("org.springframework:spring-webflux:5.3.0")

  implementation("org.springframework.data:spring-data-r2dbc:1.2.0")
  implementation("io.r2dbc:r2dbc-pool:0.8.5.RELEASE")
  implementation("io.r2dbc:r2dbc-postgresql:0.8.6.RELEASE")
  implementation("io.projectreactor.addons:reactor-pool:0.2.0")
  implementation("io.projectreactor:reactor-core:3.4.0")

//  implementation("org.postgresql:postgresql:42.2.18")

  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.0")

//  implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.3")
//  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
//  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.3")
//  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.3")

  implementation("io.micrometer:micrometer-registry-prometheus:1.5.5")
  implementation("io.prometheus:simpleclient_dropwizard:0.9.0")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}