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
package com.github.hadilq.happy.processor

import com.github.hadilq.happy.annotation.Happy
import com.github.hadilq.happy.processor.analyse.asTypeName
import com.github.hadilq.happy.processor.analyse.collectConstructorParams
import com.github.hadilq.happy.processor.analyse.findSealedParentKmClass
import com.github.hadilq.happy.processor.common.di.HappyProcessorModule
import com.github.hadilq.happy.processor.common.generate.CommonHType
import com.github.hadilq.happy.processor.common.generate.generateHappyFile
import com.github.hadilq.happy.processor.common.generate.qualifiedName
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInternal
import com.squareup.kotlinpoet.metadata.isSealed
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toKotlinClassMetadata
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.jvm.KotlinClassMetadata
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@KotlinPoetMetadataPreview
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
public class HappyProcessor : AbstractProcessor() {

  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var elementUtils: Elements

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    filer = processingEnv.filer
    messager = processingEnv.messager
    elementUtils = processingEnv.elementUtils
  }

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(Happy::class).mapTo(mutableSetOf()) { it.java.canonicalName }
  }

  override fun process(
    annotations: MutableSet<out TypeElement>,
    roundEnv: RoundEnvironment
  ): Boolean {
    roundEnv.getElementsAnnotatedWith(Happy::class.java)
      .asSequence()
      .map { it as TypeElement }
      .forEach { happyType ->
        val module = Module(
          logInfo = { messager.printMessage(Diagnostic.Kind.NOTE, it, happyType) },
          logWarning = { messager.printMessage(Diagnostic.Kind.WARNING, it, happyType) },
          logError = { messager.printMessage(Diagnostic.Kind.ERROR, it, happyType) },
        )
        val happyHType = HType(
          element = happyType,
          typeElement = { elementUtils.getTypeElement(it) },
          typeName = { typeParams -> asTypeName(typeParams) },
        )
        val sealedParentHType = findSealedParentKmClass(happyHType)
        if (sealedParentHType == null) {
          messager.printMessage(Diagnostic.Kind.ERROR, "@Happy: parent must be a sealed class!", happyType)
          return@forEach
        }
        if (module.debug) {
          messager.printMessage(Diagnostic.Kind.NOTE, "@Happy: parent class! ${sealedParentHType.meta}")
        }
        module.generateHappyFile(sealedParentHType, happyHType)
          .fold({
            it.writeTo(filer)
          }) {
            messager.printMessage(
              Diagnostic.Kind.ERROR,
              it.message,
              happyType
            )
          }
      }
    return false
  }
}

@KotlinPoetMetadataPreview
private data class Module(
  override val debug: Boolean = false,
  override val logInfo: (message: String) -> Unit,
  override val logWarning: (message: String) -> Unit,
  override val logError: (message: String) -> Unit,
) : HappyProcessorModule

@KotlinPoetMetadataPreview
public class HType(
  public val element: TypeElement,
  public val typeElement: (qualifiedName: String) -> TypeElement,
  @KotlinPoetMetadataPreview
  public val typeName: KmType?.(typeParams: List<KmTypeParameter>) -> TypeName?,
  public val meta: KmClass = element.getAnnotation(Metadata::class.java)
    .toKotlinClassMetadata<KotlinClassMetadata.Class>()
    .toKmClass(),
) : CommonHType {

  private val typeSpec: TypeSpec = meta.toTypeSpec(null)
  override val isSealed: Boolean by lazy(LazyThreadSafetyMode.NONE) { meta.flags.isSealed }
  override val isInternal: Boolean by lazy(LazyThreadSafetyMode.NONE) { meta.flags.isInternal }
  override val typeParameters: List<TypeVariableName> by lazy(LazyThreadSafetyMode.NONE) { typeSpec.typeVariables }
  override val className: TypeName by lazy(LazyThreadSafetyMode.NONE) { element.asType().asTypeName() }
  override val qualifiedName: String by lazy(LazyThreadSafetyMode.NONE) { meta.name.qualifiedName }

  override val simpleNames: List<String> by lazy(LazyThreadSafetyMode.NONE) {
    meta.name.substringAfterLast("/").split(".")
  }

  override val simpleName: String by lazy(LazyThreadSafetyMode.NONE) { qualifiedName.substringAfterLast(".") }

  override val packageName: String by lazy(LazyThreadSafetyMode.NONE) {
    meta.name.substringBeforeLast("/").replace("/", ".")
  }

  override val sealedSubclasses: Sequence<CommonHType> by lazy(LazyThreadSafetyMode.NONE) {
    meta
      .sealedSubclasses
      .map { subclass -> newType(typeElement(subclass.qualifiedName)) }
      .asSequence()
  }

  override val collectConstructorParams: Result<Pair<List<String>, List<ParameterSpec>>> by lazy(LazyThreadSafetyMode.NONE) {
    collectConstructorParams(typeName)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is HType) return false
    return element == other.element
  }

  override fun hashCode(): Int {
    return qualifiedName.hashCode()
  }

  @KotlinPoetMetadataPreview
  public fun newType(
    element: TypeElement,
    meta: KmClass = element.getAnnotation(Metadata::class.java)
      .toKotlinClassMetadata<KotlinClassMetadata.Class>()
      .toKmClass(),
  ): HType = HType(element, typeElement, typeName, meta)
}