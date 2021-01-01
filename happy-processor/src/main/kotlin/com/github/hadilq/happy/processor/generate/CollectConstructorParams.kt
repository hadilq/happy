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

import com.github.hadilq.happy.processor.di.HappyProcessorModule
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmValueParameter
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isPrimary
import kotlinx.metadata.KmClassifier

@KotlinPoetMetadataPreview
public fun HappyProcessorModule.collectConstructorParams(
  caseKmClass: ImmutableKmClass,
): Result<Pair<List<String>, List<ParameterSpec>>> = caseKmClass
  .constructors
  .asSequence()
  .filter { it.isPrimary }
  .flatMap { it.valueParameters }
  .map { param: ImmutableKmValueParameter ->
    val type = param.type
    if (type != null && type.classifier is KmClassifier.Class) {
      val paramType = ClassName.bestGuess((type.classifier as KmClassifier.Class).name.qualifiedName)

      Result.success(
        Pair(
          param.name,
          ParameterSpec.builder(param.name, paramType)
            .build()
        )
      )
    } else {
      val message = "@Happy: type of parameters must be class!"
      logError(message)
      Result.failure(RuntimeException(message))
    }
  }
  .fold(
    Result.success(
      Pair(
        mutableListOf(),
        mutableListOf()
      )
    ), { acc, pair ->
      pair.getOrNull()?.first?.let { acc.getOrNull()?.first?.add(it) }
      pair.getOrNull()?.second?.let { acc.getOrNull()?.second?.add(it) }
      acc
    })
