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
  id("com.google.devtools.ksp") version Dependencies.Ksp.version
}

setupPublication()

dependencies {
  implementation(Dependencies.AutoService.annotations)
  kapt(Dependencies.AutoService.processor)
  compileOnly(Dependencies.Incap.annotations)
  kapt(Dependencies.Incap.processor)

  compileOnly(Dependencies.Ksp.ksp)
  compileOnly(Dependencies.Ksp.api)
  compileOnly(Dependencies.Kotlin.compilerEmbeddable)

  implementation(Dependencies.KotlinPoet.kotlinPoet)
  implementation(Dependencies.KotlinPoet.ksp)
  implementation(project(":happy-annotation"))
  api(project(":happy-processor-common"))

  testImplementation(Dependencies.Ksp.ksp)
  testImplementation(Dependencies.Ksp.api)
  testImplementation(Dependencies.Kotlin.compilerEmbeddable)
  testImplementation(Dependencies.Testing.compileTesting)
  testImplementation(Dependencies.Testing.compileTestingKsp)
  testImplementation(Dependencies.Testing.truth)
  testImplementation(Dependencies.Testing.junit)
  testImplementation(Dependencies.Kotlin.reflect)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  outputs.cacheIf { false }
  kotlinOptions {
    freeCompilerArgs += listOf(
      "-Xallow-result-return-type",
      "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
    )
  }
}
