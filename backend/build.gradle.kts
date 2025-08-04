import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.Duration

plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.back"
version = "0.0.1-SNAPSHOT"

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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 도메인별 태그 테스트 태스크
val domains = listOf("user", "exchange", "trade_log", "wallet", "coin")
domains.forEach { d ->
    tasks.register<Test>("test${d.replaceFirstChar { it.uppercase() }}") {
        group = "verification"
        description = "Runs $d domain tests"
        useJUnitPlatform {
            includeTags(d)
        }
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        shouldRunAfter(tasks["test"])
    }
}

// 통합 테스트 소스세트 & 태스크
sourceSets.create("integrationTest") {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests (실제 API 호출 등)"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks["test"])
}


tasks.withType<Test>().configureEach {

    useJUnitPlatform()

    // 테스트 타임아웃 설정 (전체 테스트 실행 시간 제한)
    timeout.set(Duration.ofMinutes(10))

    // JVM 설정 - 메모리 최적화
    val isCI = System.getenv("CI") == "true" || System.getenv("GITHUB_ACTIONS") == "true"

    if (isCI) {
        // CI 환경: 리소스 제한
        jvmArgs(
            "-Xmx1g",  // 메모리를 1GB로 제한
            "-XX:+UseG1GC"
        )

        // 테스트 병렬 실행 제한 - CI 환경에서만
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
        systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
        systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "2")  // 최대 2개 병렬
        systemProperty("junit.jupiter.execution.parallel.config.fixed.max-pool-size", "2")
    } else {
        // 로컬 환경: 더 많은 리소스 활용
        jvmArgs(
            "-Xmx2g",  // 로컬에서는 2GB 사용
            "-XX:+UseG1GC"
        )

        // 로컬에서는 동적 병렬 실행 (CPU 코어 수에 따라)
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
        systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")
    }

    // 실패 테스트만 모아 마지막에 요약 출력
    val failed = mutableListOf<Pair<TestDescriptor, TestResult>>()
    val taskName = name

    // 콘솔 출력 최소화: 실패만, 표준 출력 안 찍기
    testLogging {
        events("FAILED") // PASSED/Skipped/STDOUT 등은 끔
        exceptionFormat = TestExceptionFormat.SHORT // FULL로 바꾸면 전체 스택
        showCauses = true
        showStackTraces = true
        showStandardStreams = false
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            if (result.resultType == TestResult.ResultType.FAILURE) {
                failed += testDescriptor to result
            }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) {
                // 최상위(태스크) 요약만 출력
                println("── $taskName summary ───────────────────────────")
                println("Result  : ${result.resultType}")
                println("Tests   : ${result.testCount},  Failed : ${result.failedTestCount}")
                println("──────────────────────────────────────────────")

                if (failed.isNotEmpty()) {
                    println("❌ Failed tests (${failed.size}):")
                    failed.forEach { (d, r) ->
                        val ex = r.exceptions.firstOrNull()
                        println(" - ${d.className}.${d.name}")
                        if (ex != null) {
                            // 실패 이유만 간단히
                            println("   ${ex.javaClass.simpleName}: ${ex.message}")
                        }
                    }
                    println("──────────────────────────────────────────────")
                }
            }
        }
    })
}