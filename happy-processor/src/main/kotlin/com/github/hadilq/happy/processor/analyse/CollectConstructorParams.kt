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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isPrimary
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter

@KotlinPoetMetadataPreview
public fun HType.collectConstructorParams(
  typeName: KmType?.(typeParams: List<KmTypeParameter>) -> TypeName?,
): Result<Pair<List<String>, List<ParameterSpec>>> = meta
  .constructors
  .asSequence()
  .filter { it.isPrimary }
  .flatMap { it.valueParameters }
  .flatMap { param: KmValueParameter ->
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
        val message = "@Happy: Unknown error happened!"
        sequenceOf(Result.failure(RuntimeException(message)))
      }
    }
  }
  .fold(
    Result.success(
      Pair(
        mutableListOf(),
        mutableListOf(),
      )
    )
  ) { acc: Result<Pair<MutableList<String>, MutableList<ParameterSpec>>>, result ->
    result.getOrNull()?.let { pair ->
      acc.getOrNull()!!.apply {
        first.add(pair.first)
        second.add(pair.second)
      }
      acc
    } ?: Result.failure(result.exceptionOrNull()!!)
  }

@KotlinPoetMetadataPreview
private fun TypeName?.generateParamSpecs(
  param: KmValueParameter,
  isVararg: Boolean,
): Sequence<Result<Pair<String, ParameterSpec>>> = this?.let { type ->
  val builder = ParameterSpec.builder(param.name, type)
  if (isVararg) {
    builder.addModifiers(KModifier.VARARG)
  }
  sequenceOf(Result.success(Pair(param.name, builder.build())))
} ?: run {
  val message = "@Happy: Unknown error happened!"
  sequenceOf(Result.failure(RuntimeException(message)))
}