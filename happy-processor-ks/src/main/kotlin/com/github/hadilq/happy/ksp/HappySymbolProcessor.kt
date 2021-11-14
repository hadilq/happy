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
package com.github.hadilq.happy.ksp

import com.github.hadilq.happy.annotation.Happy
import com.github.hadilq.happy.processor.common.di.HappyProcessorModule
import com.github.hadilq.happy.ksp.analyse.collectConstructorParams
import com.github.hadilq.happy.ksp.analyse.findSealedParent
import com.github.hadilq.happy.processor.common.generate.generateHappyFile
import com.github.hadilq.happy.processor.common.generate.CommonHType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

public class HappySymbolProcessor(
  environment: SymbolProcessorEnvironment
) : SymbolProcessor {

  private companion object {
    val HAPPY_CLASS_NAME = Happy::class.qualifiedName!!
  }

  private val codeGenerator = environment.codeGenerator
  private val logger = environment.logger

  override fun process(resolver: Resolver): List<KSAnnotated> {

    resolver.getClassDeclarationByName(
      resolver.getKSNameFromString(HAPPY_CLASS_NAME)
    ) ?: run {
      logger.error("@Happy type not found on the classpath.")
      return emptyList()
    }

    resolver.getSymbolsWithAnnotation(HAPPY_CLASS_NAME)
      .forEach { happyType ->
        if (happyType !is KSClassDeclaration) {
          logger.error("@HappyClass can't be applied to $happyType: must be a Kotlin class", happyType)
          return@forEach
        }

        val module = Module(
          logInfo = { logger.info(it, happyType) },
          logWarning = { logger.warn(it, happyType) },
          logError = { logger.error(it, happyType) },
        )

        val happyHType = HType(happyType)
        val sealedParentHType = findSealedParent(happyHType)
        if (sealedParentHType == null) {
          logger.error("@Happy: parent must be a sealed class!", happyType)
          return@forEach
        }
        if (module.debug) {
          logger.info("@Happy: parent class! ${sealedParentHType.declaration}")
        }
        module.generateHappyFile(sealedParentHType, happyHType)
          .fold({
            it.writeTo(codeGenerator, aggregating = false)
          }) {
            logger.error(
              it.message!!,
              happyType
            )
          }
      }
    return emptyList()
  }
}

private data class Module(
  override val debug: Boolean = false,
  override val logInfo: (message: String) -> Unit,
  override val logWarning: (message: String) -> Unit,
  override val logError: (message: String) -> Unit,
) : HappyProcessorModule


public class HType(
  public val declaration: KSClassDeclaration
) : CommonHType {
  override val isSealed: Boolean by lazy {
    Modifier.SEALED in declaration.modifiers
  }
  override val isInternal: Boolean by lazy {
    Modifier.INTERNAL in declaration.modifiers
  }
  override val typeParameters: List<TypeVariableName> by lazy {
    declaration.typeParameters.map { it.toTypeVariableName(declaration.typeParameters.toTypeParameterResolver()) }
  }
  override val className: TypeName by lazy { declaration.toClassName() }
  override val qualifiedName: String? by lazy { declaration.qualifiedName?.getQualifier() }
  override val simpleNames: List<String> by lazy {
    declaration.simpleName.getQualifier().substringAfterLast("/").split(".")
  }
  override val simpleName: String by lazy { declaration.simpleName.asString() }
  override val packageName: String by lazy {
    declaration.simpleName.getQualifier().substringBeforeLast("/").replace("/", ".")
  }
  override val sealedSubclasses: Sequence<CommonHType> by lazy { declaration.getSealedSubclasses().map { HType(it) } }
  override val collectConstructorParams: Result<Pair<List<String>, List<ParameterSpec>>> by lazy {
    collectConstructorParams(declaration)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is HType) return false
    return declaration == other.declaration
  }
}