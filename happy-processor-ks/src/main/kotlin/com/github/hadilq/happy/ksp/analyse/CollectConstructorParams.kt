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

import com.github.hadilq.happy.processor.common.generate.CollectConstructorParams
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.github.hadilq.happy.processor.common.generate.elvis

public fun collectConstructorParams(
  declaration: KSClassDeclaration,
): CollectConstructorParams = declaration
  .primaryConstructor
  ?.parameters
  ?.map { param: KSValueParameter ->
    val ksType = param.type.resolve()
    ksType.toTypeName(declaration.typeParameters.toTypeParameterResolver())
      .generateParamSpecs(param)
  }
  ?.fold(
    CollectConstructorParams.Params(emptyList(), emptyList())
  ) fold@{ acc: CollectConstructorParams, result ->  // For the lack of traverse for sequences
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
  ?: CollectConstructorParams.Failure.NoPrimaryConstructor(
    throwable = RuntimeException("@Happy: $declaration doesn't have a primary constructor!")
  )

private fun TypeName?.generateParamSpecs(
  param: KSValueParameter,
): CollectConstructorParams = this?.let { type ->
  val name = param.name?.asString()
    ?: return@let CollectConstructorParams.Failure.NoParam(RuntimeException("@Happy: the param, $param, should have a name!"))
  val builder = ParameterSpec.builder(name, type)
  if (param.isVararg) {
    builder.addModifiers(KModifier.VARARG)
  }
  CollectConstructorParams.Params(listOf(name), listOf(builder.build()))
} ?: run {
  val message = "@Happy: Unknown error happened!"
  CollectConstructorParams.Failure.Unknown(RuntimeException(message))
}
