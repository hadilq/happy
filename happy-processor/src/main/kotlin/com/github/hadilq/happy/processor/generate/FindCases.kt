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
package com.github.hadilq.happy.processor.generate

import com.github.hadilq.happy.processor.HType
import com.github.hadilq.happy.processor.di.HappyProcessorModule
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.TypeElement

@KotlinPoetMetadataPreview
public fun HappyProcessorModule.findCases(
  sealedParentHType: HType,
  happyHType: HType,
): Sequence<Pair<List<String>, HType>> = sealedParentHType.meta.sealedSubclasses
  .asSequence()
  .map { Pair(emptyList<String>(), it.qualifiedName) }
  .mapNestedCases(this, happyHType.element)

@KotlinPoetMetadataPreview
private fun HappyProcessorModule.findNestedCases(
  parentHType: HType,
  happyType: TypeElement,
  parentsName: List<String>
): Sequence<Pair<List<String>, HType>> = parentHType.meta.nestedClasses
  .asSequence()
  .map { Pair(parentsName, "${parentHType.qualifiedName}.$it") }
  .mapNestedCases(this, happyType)

@KotlinPoetMetadataPreview
private fun Sequence<Pair<List<String>, String>>.mapNestedCases(
  module: HappyProcessorModule,
  happyType: TypeElement,
): Sequence<Pair<List<String>, HType>> = this
  .map { Pair(it.first, module.typeElement(it.second)) }
  .filter { it.second != happyType }
  .map { Pair(it.first, HType(it.second)) }
  .flatMap {
    val parentsName = mutableListOf(*it.first.toTypedArray())
    parentsName.add(it.second.simpleName)
    return@flatMap if (it.second.meta.nestedClasses.isEmpty()) {
      if (module.debug) {
        module.logNote("parentsName: $parentsName")
        module.logNote("found case: ${it.second}")
      }
      sequenceOf(Pair(parentsName, it.second))
    } else {
      module.findNestedCases(it.second, happyType, parentsName)
    }
  }
