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

plugins {
  kotlin("jvm")
  kotlin("kapt")
}


dependencies {
  kapt(project(":happy-processor"))
  compileOnly(project(":happy-annotation"))

  kaptTest(project(":happy-processor"))
  testCompileOnly(project(":happy-annotation"))
  testImplementation(Dependencies.Testing.junit)
  testImplementation(Dependencies.Testing.truth)
}

val generatedAnnotation = if (JavaVersion.current().isJava10Compatible) {
  "javax.annotation.processing.Generated"
} else {
  "javax.annotation.Generated"
}

kapt {
  arguments {
    arg("happy.generated", generatedAnnotation)
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  outputs.cacheIf { false }
  kotlinOptions {
    @Suppress("SuspiciousCollectionReassignment")
    freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
  }
}
