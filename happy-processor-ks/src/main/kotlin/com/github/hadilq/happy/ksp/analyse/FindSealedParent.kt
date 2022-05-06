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

import com.github.hadilq.happy.ksp.HType
import com.github.hadilq.happy.processor.common.di.HappyProcessorModule
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal fun HappyProcessorModule.findSealedParent(
  hType: HType
): HType? {
  val superClass = hType.declaration.superTypes
    .map { it.resolve().declaration }
    .filterIsInstance<KSClassDeclaration>()
    .firstOrNull {
      it.classKind == ClassKind.CLASS || it.classKind == ClassKind.INTERFACE
    } ?: return if (hType.isSealed) hType else null

  logInfo("qualifiedName: ${superClass.qualifiedName?.asString()}, isSealed: ${hType.isSealed}")
  if (superClass.qualifiedName?.asString() == "kotlin.Any") {
    return if (hType.isSealed) hType else null
  }
  return findSealedParent(HType(superClass))
}
