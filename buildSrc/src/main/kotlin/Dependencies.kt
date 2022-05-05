/*
 * Copyright 2021 Hadi Lashkari Ghouchani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

object Dependencies {

  object AutoService {
    private const val version = "1.0"
    const val annotations = "com.google.auto.service:auto-service-annotations:$version"
    const val processor = "com.google.auto.service:auto-service:$version"
  }

  object Incap {
    private const val version = "0.3"
    const val annotations = "net.ltgt.gradle.incap:incap:$version"
    const val processor = "net.ltgt.gradle.incap:incap-processor:$version"
  }

  object Kotlin {
    const val version = "1.6.21"
    const val dokkaVersion = "1.6.21"
    const val jvmTarget = "1.8"
    const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$version"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
    val defaultFreeCompilerArgs = listOf("-Xjsr305=strict", "-progressive")
  }

  object Ksp {
    const val version = "1.6.21-1.0.5"
    const val ksp = "com.google.devtools.ksp:symbol-processing:$version"
    const val api = "com.google.devtools.ksp:symbol-processing-api:$version"
    const val plugin = "com.google.devtools.ksp"
  }

  object KotlinPoet {
    private const val version = "1.10.2"
    const val kotlinPoet = "com.squareup:kotlinpoet:$version"
    const val metadata = "com.squareup:kotlinpoet-metadata:$version"
    const val ksp = "com.squareup:kotlinpoet-ksp:$version"
  }

  object LatestHappy {
    const val version = "0.1.0"
    const val happyFakeGroup = "com.fake.happy"
    const val happyProcessorKaptPackage = "com.github.hadilq:happy-processor"
    const val happyProcessorKspPackage = "com.github.hadilq:happy-processor-ks"
    const val happyAnnotationPackage = "com.github.hadilq:happy-annotation"
    const val happyCommonFakePackage = "$happyFakeGroup:happy-processor-common"
    const val happyProcessorKsp = "$happyProcessorKspPackage:$version"
    const val happyAnnotation = "$happyAnnotationPackage:$version"
    const val happyCommonFake = "$happyCommonFakePackage:$version"
  }

  object Testing {
    private const val compiletTestingVersion = "1.4.7"
    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:$compiletTestingVersion"
    const val compileTestingKsp = "com.github.tschuchortdev:kotlin-compile-testing-ksp:$compiletTestingVersion"
    const val junit = "junit:junit:4.13.1"
    const val truth = "com.google.truth:truth:1.1.3"
  }
}
