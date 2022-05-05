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

import com.squareup.kotlinpoet.*

public fun Sequence<Pair<List<String>, CommonHType>>.generateBuilderFunctions(
  happyHType: CommonHType,
): Sequence<Result<FunSpec>> =
  map { (names, caseClass) ->
    val constructorParams = caseClass.collectConstructorParams
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
            """.trimIndent(), caseClass.className
          )
        )
        .build()
    )
  }

public sealed interface GenerateBuilderFunction {

  @Happy
  public data class Function(
    val function: FunSpec
  ): GenerateBuilderFunction

  public data class NoPrimaryConstructor(val classQualifiedName: String?) : GenerateBuilderFunction
  public object NoParamName: GenerateBuilderFunction
  public object Unknown: GenerateBuilderFunction
}