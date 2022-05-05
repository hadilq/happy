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
  id(Dependencies.Ksp.plugin) version Dependencies.Ksp.version
  kotlin("jvm")
  kotlin("kapt")
}

val kspEnabled = findProperty("happy.ksp.enable")?.toString()?.toBoolean() ?: false
val libVersion = findProperty("happy.snapshot.version")?.toString() ?: ""

dependencies {

  if (kspEnabled) {
    if (libVersion.isNotBlank()) {
      ksp("${Dependencies.LatestHappy.happyProcessorKspPackage}:$libVersion")
      kspTest("${Dependencies.LatestHappy.happyProcessorKspPackage}:$libVersion")
    } else {
      ksp(project(":happy-processor-ks"))
      kspTest(project(":happy-processor-ks"))
    }
  } else {
    if (libVersion.isNotBlank()) {
      kapt("${Dependencies.LatestHappy.happyProcessorKaptPackage}:$libVersion")
      kaptTest("${Dependencies.LatestHappy.happyProcessorKaptPackage}:$libVersion")
    } else {
      kapt(project(":happy-processor"))
      kaptTest(project(":happy-processor"))
    }
  }

  if (libVersion.isNotBlank()) {
    compileOnly("${Dependencies.LatestHappy.happyAnnotationPackage}:$libVersion")
    testCompileOnly("${Dependencies.LatestHappy.happyAnnotationPackage}:$libVersion")
  } else {
    compileOnly(project(":happy-annotation"))
    testCompileOnly(project(":happy-annotation"))
  }

  testImplementation(Dependencies.Testing.junit)
  testImplementation(Dependencies.Testing.truth)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  outputs.cacheIf { false }
  kotlinOptions {
    suppressWarnings = true
    @Suppress("SuspiciousCollectionReassignment")
    freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
  }
}
