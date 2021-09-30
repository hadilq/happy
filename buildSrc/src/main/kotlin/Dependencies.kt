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
    const val version = "1.4.20"
    const val dokkaVersion = "1.4.20"
    const val jvmTarget = "1.8"
    val defaultFreeCompilerArgs = listOf("-Xjsr305=strict", "-progressive")
  }

  object KotlinPoet {
    private const val version = "1.9.0"
    const val kotlinPoet = "com.squareup:kotlinpoet:$version"
    const val metadata = "com.squareup:kotlinpoet-metadata-specs:$version"
  }

  object Testing {
    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.4.4"
    const val junit = "junit:junit:4.13.1"
    const val truth = "com.google.truth:truth:1.1.3"
  }
}
