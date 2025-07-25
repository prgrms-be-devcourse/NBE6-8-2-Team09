import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
    // 스프링 부트 스타터 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // 데이터베이스 연동
    runtimeOnly("com.h2database:h2")
    
    // 롬복
    annotationProcessor("org.projectlombok:lombok")
    
    // 테스트 관련 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


//   태그 도메인 별 실행
// 추가 필요할 시 list 안에 도메인 이름 추가
val domains = listOf("user", "exchange", "trade_log", "wallet", "coin")

domains.forEach { d ->
    tasks.register<Test>("test${d.replaceFirstChar { it.uppercase() }}") {
        group = "verification"
        description = "Runs $d domain tests"
        useJUnitPlatform {
            includeTags(d)        // 해당 태그만 실행
        }
        // 기존 test 소스세트와 클래스패스 공유
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath       = sourceSets["test"].runtimeClasspath
        shouldRunAfter(tasks["test"])   // 필요 없다면 제거
    }
}

// ─────────────────────────────────────────────────────────────
// 통합 테스트(integrationTest) 소스세트 & 태스크
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
    classpath       = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks["test"])
}

tasks.withType<Test>().configureEach {

    useJUnitPlatform()

    val failed = mutableListOf<Pair<TestDescriptor, TestResult>>()
    val taskName = name

    testLogging {
        events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT", "STANDARD_ERROR")
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
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
            // suite.parent == null == 최상위 스위트(태스크) 요약
            if (suite.parent == null) {
                println(
                    """
                    ── $taskName summary ───────────────────────────
                    Result  : ${result.resultType}
                    Tests   : ${result.testCount}
                    Passed  : ${result.successfulTestCount}
                    Failed  : ${result.failedTestCount}
                    Skipped : ${result.skippedTestCount}
                    ──────────────────────────────────────────────
                    """.trimIndent()
                )

                if (failed.isNotEmpty()) {
                    println("❌ Failed tests detail (${failed.size}):")
                    failed.forEach { (d, r) ->
                        val ex = r.exceptions.firstOrNull()
                        println(" - ${d.className}.${d.name}")
                        if (ex != null) {
                            println("   message : ${ex.message}")
                            println("   cause   : ${ex.cause}")
                            ex.stackTrace.take(20).forEach { ste ->
                                println("     at $ste")
                            }
                            if (ex.stackTrace.size > 20) {
                                println("     ... (${ex.stackTrace.size - 20} more)")
                            }
                        }
                    }
                    println("──────────────────────────────────────────────")
                }
            }
        }
    })
}