
plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.6"
  kotlin("plugin.spring") version "1.4.10"
  id("org.unbroken-dome.xjc") version "2.0.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

val jaxbVersion by extra { "2.3.1" }
val lombokVersion by extra { "1.18.6" }
val junitJupiterVersion by extra { "5.4.2" }

dependencies {

  compileOnly("org.projectlombok:lombok:$lombokVersion")
  annotationProcessor("org.projectlombok:lombok:$lombokVersion")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("wsdl4j:wsdl4j")
  implementation("javax.xml.bind:jaxb-api:$jaxbVersion")

  runtimeOnly("javax.xml.bind:jaxb-api:$jaxbVersion")
  runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")

  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation("org.springframework.ws:spring-ws-test")
  testImplementation("junit:junit:4.12")
  testImplementation("org.mockito:mockito-core")
}

xjc {
  srcDirName.set("resources/xsd")
  extension.set(true)
}

sourceSets.named("main") {
  xjcBinding.srcDir("resources/xsd")
}
