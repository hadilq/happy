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
package com.github.hadilq.happy.ksp.analyse

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

public fun collectConstructorParams(
  declaration: KSClassDeclaration,
): Result<Pair<List<String>, List<ParameterSpec>>> = declaration
  .primaryConstructor
  ?.parameters
  ?.map { param: KSValueParameter ->
    val ksType = param.type.resolve()
    ksType.toTypeName(ksType.declaration.typeParameters.toTypeParameterResolver())
      .generateParamSpecs(param)
  }
  ?.fold(
    Result.success(
      Pair(
        mutableListOf(),
        mutableListOf(),
      )
    )
  ) { acc: Result<Pair<MutableList<String>, MutableList<ParameterSpec>>>, result ->  // For the lack of traverse for sequences
    acc.fold({ a ->
      result.fold({ pair ->
        a.apply {
          first.add(pair.first)
          second.add(pair.second)
        }
        Result.success(a)
      }) { Result.failure(it) }
    }) { Result.failure(it) }
  } ?: Result.failure(RuntimeException("@Happy: $declaration doesn't have a primary constructor!"))

private fun TypeName?.generateParamSpecs(
  param: KSValueParameter,
): Result<Pair<String, ParameterSpec>> = this?.let { type ->
  val name = param.name?.asString()
    ?: return@let Result.failure(RuntimeException("@Happy: they param, $param, should have a name!"))
  val builder = ParameterSpec.builder(name, type)
  if (param.isVararg) {
    builder.addModifiers(KModifier.VARARG)
  }
  Result.success(Pair(name, builder.build()))
} ?: run {
  val message = "@Happy: Unknown error happened!"
  Result.failure(RuntimeException(message))
}
