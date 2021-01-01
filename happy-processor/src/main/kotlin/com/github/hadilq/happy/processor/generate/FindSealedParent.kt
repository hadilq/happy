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

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isSealed
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

@KotlinPoetMetadataPreview
public fun findSealedParentKmClass(
  kmClass: ImmutableKmClass,
  type: TypeElement
): ImmutableKmClass? {
  repeat(kmClass.supertypes.size) found@{
    val superClass: DeclaredType = type.superclass as? DeclaredType ?: return null
    val superTypeElement = superClass.asElement() as? TypeElement ?: return null
    if (superTypeElement.qualifiedName.toString() == "java.util.Object") return null
    val supperKmClass = superTypeElement
      .getAnnotation(Metadata::class.java)
      ?.toImmutableKmClass() ?: return null
    return if (supperKmClass.isSealed) {
      supperKmClass
    } else {
      findSealedParentKmClass(supperKmClass, superTypeElement)
    }
  }
  return null
}
