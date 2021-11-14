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
package com.github.hadilq.happy.processor.common.generate

import com.github.hadilq.happy.processor.common.di.HappyProcessorModule

public fun HappyProcessorModule.findCases(
  sealedParentHType: CommonHType,
  happyHType: CommonHType,
): Sequence<Pair<List<String>, CommonHType>> = sealedParentHType.sealedSubclasses
  .map { Pair(emptyList<String>(), it) }
  .mapNestedCases(this, happyHType)

private fun Sequence<Pair<List<String>, CommonHType>>.mapNestedCases(
  module: HappyProcessorModule,
  happyType: CommonHType,
): Sequence<Pair<List<String>, CommonHType>> = this
  .map { Pair(it.first, it.second) }
  .filter { it.second != happyType }
  .flatMap { pair ->
    val parentsName = mutableListOf(*pair.first.toTypedArray())
    parentsName.add(pair.second.simpleName)
    pair.second.sealedSubclasses
      .map { Pair(parentsName, it) }
      .mapNestedCases(module, happyType)
      .ifEmpty {
        if (module.debug) {
          module.logInfo("parentsName: $parentsName")
          module.logInfo("found case: ${pair.second}")
        }
        sequenceOf(Pair(parentsName, pair.second))
      }
  }
