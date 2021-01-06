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
package com.github.hadilq.happy.processor.di

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.ImmutableKmTypeParameter
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.TypeElement

public interface HappyProcessorModule {
  public val debug: Boolean
  public val logNote: (message: String) -> Unit
  public val logWarning: (message: String) -> Unit
  public val logError: (message: String) -> Unit
  public val typeElement: (qualifiedName: String) -> TypeElement

  @KotlinPoetMetadataPreview
  public val typeName: ImmutableKmType?.(typeParams: List<ImmutableKmTypeParameter>) -> TypeName?
}
