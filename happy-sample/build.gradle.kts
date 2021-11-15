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

dependencies {
  if (kspEnabled) {
    ksp(project(":happy-processor-ks"))
    kspTest(project(":happy-processor-ks"))
  } else {
    kapt(project(":happy-processor"))
    kaptTest(project(":happy-processor"))
  }
  compileOnly(project(":happy-annotation"))

  testCompileOnly(project(":happy-annotation"))
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
