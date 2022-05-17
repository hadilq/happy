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
import com.github.hadilq.happy.processor.common.di.HappyProcessorModule
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInterface
import com.squareup.kotlinpoet.metadata.toKotlinClassMetadata
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

@KotlinPoetMetadataPreview
internal fun HappyProcessorModule.findSealedParentKmClass(
  hType: HType,
): HType? {
  if (hType.meta.flags.isInterface && hType.isSealed) {
    return hType
  }
  return mutableListOf(hType.element.superclass).apply { addAll(hType.element.interfaces) }
    .mapNotNull { it as? DeclaredType }
    .mapNotNull { it.asElement() as? TypeElement }
    .mapNotNull { superClass ->
      logInfo("super class: ${superClass.qualifiedName}")
      if (superClass.qualifiedName.toString() == "java.lang.Object") {
        return@mapNotNull hType
      }
      val superKmClass = superClass
        .getAnnotation(Metadata::class.java)
        ?.toKotlinClassMetadata<KotlinClassMetadata.Class>()
        ?.toKmClass()

      logInfo("super KM class: $superKmClass")

      superKmClass
        ?.let { findSealedParentKmClass(hType.newType(superClass, it)) }
    }
    .firstOrNull { it.isSealed }
}
