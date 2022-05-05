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

import com.github.hadilq.happy.annotation.Happy
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

public interface CommonHType {
  public val isSealed: Boolean
  public val isInternal: Boolean
  public val typeParameters: List<TypeVariableName>
  public val className: TypeName
  public val qualifiedName: String?
  public val simpleNames: List<String>
  public val simpleName: String
  public val packageName: String
  public val sealedSubclasses: Sequence<CommonHType>
  public val collectConstructorParams: Result<Pair<List<String>, List<ParameterSpec>>>
  public override operator fun equals(other: Any?): Boolean
}

public sealed interface CollectConstructorParams {

  @Happy
  public data class Params(
    val namesList: List<String>,
    val paramsList: List<ParameterSpec>,
  ): CollectConstructorParams

  public object NoPrimaryConstructor : CollectConstructorParams
  public object NoParamName: CollectConstructorParams
  public object Unknown: CollectConstructorParams
}
