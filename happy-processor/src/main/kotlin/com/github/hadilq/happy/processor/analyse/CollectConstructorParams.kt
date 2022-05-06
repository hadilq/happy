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
package com.github.hadilq.happy.processor.analyse

import com.github.hadilq.happy.processor.HType
import com.github.hadilq.happy.processor.common.generate.CollectConstructorParams
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isPrimary
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter
import com.github.hadilq.happy.processor.common.generate.elvis

@KotlinPoetMetadataPreview
public fun HType.collectConstructorParams(
  typeName: KmType?.(typeParams: List<KmTypeParameter>) -> TypeName?,
): CollectConstructorParams = meta
  .constructors
  .asSequence()
  .filter { it.isPrimary }
  .flatMap { it.valueParameters }
  .map { param: KmValueParameter ->
    when {
      param.type != null -> {
        param.type.typeName(meta.typeParameters)
          .generateParamSpecs(param, false)
      }
      param.varargElementType != null -> {
        param.varargElementType.typeName(meta.typeParameters)
          .generateParamSpecs(param, true)
      }
      else -> {
        val message = "@Happy: param type is not supported!"
        CollectConstructorParams.Failure.NoParam(RuntimeException(message))
      }
    }
  }
  .fold(
    CollectConstructorParams.Params(emptyList(), emptyList())
  ) { acc: CollectConstructorParams, result ->
    val previousSuccesses = acc.elvis(
      FailureNoPrimaryConstructor = { return@fold it },
      FailureNoParam = { return@fold it },
      FailureUnknown = { return@fold it },
    )
    val newSuccess = result.elvis(
      FailureNoPrimaryConstructor = { return@fold it },
      FailureNoParam = { return@fold it },
      FailureUnknown = { return@fold it },
    )
    CollectConstructorParams.Params(
      namesList = previousSuccesses.namesList + newSuccess.namesList,
      paramsList = previousSuccesses.paramsList + newSuccess.paramsList,
    )
  }

@KotlinPoetMetadataPreview
private fun TypeName?.generateParamSpecs(
  param: KmValueParameter,
  isVararg: Boolean,
): CollectConstructorParams = this?.let { type ->
  val builder = ParameterSpec.builder(param.name, type)
  if (isVararg) {
    builder.addModifiers(KModifier.VARARG)
  }
  CollectConstructorParams.Params(listOf(param.name), listOf(builder.build()))
} ?: run {
  val message = "@Happy: Unknown error happened!"
  CollectConstructorParams.Failure.Unknown(RuntimeException(message))
}
