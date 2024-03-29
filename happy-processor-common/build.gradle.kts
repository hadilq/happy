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
}

setupPublication()

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}


repositories {
  /**
   * We need this `fakeGroup` to resolve the latest published library
   * directly from maven central and avoid omitting it like below!
   * ```
   * kspKotlinProcessorClasspath
   * +--- com.github.hadilq:happy-processor-common:*** -> project :happy-processor-common (*)
   *
   * ...
   *
   * (*) - dependencies omitted (listed previously)
   * ```
   * To see a similar log just run `./gradlew :happy-processor-common:dependencies`.
   */
  val fakeGroup = ivy {
    url = uri("https://oss.sonatype.org/content/repositories/snapshots/com/github/hadilq")

    patternLayout {
      artifact("[module]/[revision]/[module]-0.1.1.1651874075489-20220517.225728-1.jar")
    }
    metadataSources { artifact() }
  }
  exclusiveContent {
    forRepositories(fakeGroup)
    filter { includeGroup(Dependencies.LatestHappy.happyFakeGroup) }
  }
}

dependencies {
  implementation(Dependencies.KotlinPoet.kotlinPoet)
  implementation(Dependencies.KotlinPoet.metadata)
  implementation(Dependencies.LatestHappy.happyAnnotationSnapshot)
  ksp(Dependencies.LatestHappy.happyCommonFake)
  ksp(Dependencies.LatestHappy.happyProcessorSnapshotKsp)
}
