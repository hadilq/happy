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
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
public fun HappyProcessorModule.generateBuilderFunctions(
  happyHType: HType,
  cases: List<Pair<List<String>, HType>>,
): Sequence<Result<FunSpec>> = cases
  .asSequence()
  .map { (names, caseClass) ->
    val constructorParams = collectConstructorParams(caseClass)
    val (paramsName: List<String>, paramsSpec: List<ParameterSpec>) =
      constructorParams.getOrNull() ?: return@map Result.failure(constructorParams.exceptionOrNull()!!)

    Result.success(
      FunSpec.builder(names.joinToString(""))
        .addModifiers(KModifier.PUBLIC)
        .addModifiers(KModifier.INLINE)
        .addParameter(
          BLOCK_NAME, LambdaTypeName.get(
            parameters = paramsSpec,
            returnType = happyHType.className
          )
        )
        .addCode(
          CodeBlock.of(
            """
              if($SEALED_PROPERTY_NAME is %T) {
                $RESULT_VAR_NAME = $BLOCK_NAME(${paramsName.joinToString(", ") { "$SEALED_PROPERTY_NAME.$it" }})
              }
            """.trimIndent(), typeElement(caseClass.qualifiedName)
          )
        )
        .build()
    )
  }
