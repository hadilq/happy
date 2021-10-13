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
import com.github.hadilq.happy.processor.HappyProcessor.Companion.OPTION_GENERATED
import com.github.hadilq.happy.processor.di.HappyProcessorModule
import com.github.hadilq.happy.processor.generate.*
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
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
@SupportedOptions(OPTION_GENERATED)
@AutoService(Processor::class)
public class HappyProcessor : AbstractProcessor() {

  public companion object {
    /**
     * This annotation processing argument can be specified to have a `@Generated` annotation
     * included in the generated code. It is not encouraged unless you need it for static analysis
     * reasons and not enabled by default.
     *
     * Note that this can only be one of the following values:
     *   * `"javax.annotation.processing.Generated"` (JRE 9+)
     *   * `"javax.annotation.Generated"` (JRE <9)
     */
    public const val OPTION_GENERATED: String = "happy.generated"

    private val POSSIBLE_GENERATED_NAMES = setOf(
      "javax.annotation.processing.Generated",
      "javax.annotation.Generated"
    )
  }

  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var elementUtils: Elements
  private var generatedAnnotation: AnnotationSpec? = null

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    filer = processingEnv.filer
    messager = processingEnv.messager
    elementUtils = processingEnv.elementUtils
    generatedAnnotation = processingEnv.options[OPTION_GENERATED]?.let {
      require(it in POSSIBLE_GENERATED_NAMES) {
        "Invalid option value for $OPTION_GENERATED. Found $it, allowable values are $POSSIBLE_GENERATED_NAMES."
      }
      elementUtils.getTypeElement(it)
    }?.let {
      @Suppress("DEPRECATION")
      AnnotationSpec.builder(it.asClassName())
        .addMember("value = [%S]", HappyProcessor::class.java.canonicalName)
        .addMember("comments = %S", "https://github.com/hadilq/happy-path")
        .build()
    }
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
          logNote = { messager.printMessage(Diagnostic.Kind.NOTE, it, happyType) },
          logWarning = { messager.printMessage(Diagnostic.Kind.WARNING, it, happyType) },
          logError = { messager.printMessage(Diagnostic.Kind.ERROR, it, happyType) },
          typeElement = { elementUtils.getTypeElement(it) },
          typeName = { typeParams -> asTypeName(typeParams) },
        )
        val happyHType = HType(happyType)
        val sealedParentHType = findSealedParentKmClass(happyHType)
        if (sealedParentHType == null) {
          messager.printMessage(Diagnostic.Kind.ERROR, "@Happy: parent must be a sealed class!", happyType)
          return@forEach
        }
        if (module.debug) {
          messager.printMessage(Diagnostic.Kind.NOTE, "@Happy: parent class! ${sealedParentHType.meta}")
        }
        val result = module.generateHappyFile(sealedParentHType, happyHType)
        result
          .getOrNull()
          ?.writeTo(filer)
          ?: messager.printMessage(
            Diagnostic.Kind.ERROR,
            result.exceptionOrNull()!!.message,
            happyType
          )
      }
    return false
  }
}

@KotlinPoetMetadataPreview
private data class Module(
  override val debug: Boolean = false,
  override val logNote: (message: String) -> Unit,
  override val logWarning: (message: String) -> Unit,
  override val logError: (message: String) -> Unit,
  override val typeElement: (qualifiedName: String) -> TypeElement,
  override val typeName: KmType?.(typeParams: List<KmTypeParameter>) -> TypeName?,
) : HappyProcessorModule

@KotlinPoetMetadataPreview
public data class HType(
  val element: TypeElement,
  val meta: KmClass = element.getAnnotation(Metadata::class.java)
    .toKotlinClassMetadata<KotlinClassMetadata.Class>()
    .toKmClass(),
  val typeSpec: TypeSpec = meta.toTypeSpec(null),
  val typeParameters: List<TypeVariableName> = typeSpec.typeVariables,
  val className: TypeName = element.asType().asTypeName(),
  val qualifiedName: String = meta.name.qualifiedName,
  val simpleNames: List<String> = meta.name.substringAfterLast("/").split("."),
  val simpleName: String = qualifiedName.substringAfterLast("."),
  val packageName: String = meta.name.substringBeforeLast("/").replace("/", "."),
)
