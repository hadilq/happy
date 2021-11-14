package com.github.hadilq.happy.processor.analyse

import com.github.hadilq.happy.processor.common.generate.qualifiedName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter

@KotlinPoetMetadataPreview
public fun KmType?.asTypeName(
  typeParams: List<KmTypeParameter>,
): TypeName? {
  val type: KmType = this ?: return null
  return when (val classifier = type.classifier) {
    is KmClassifier.Class -> {
      if (type.arguments.isEmpty()) {
        ClassName.bestGuess(classifier.name.qualifiedName)
      } else {
        ClassName.bestGuess(classifier.name.qualifiedName)
          .parameterizedBy(type.arguments.flatMap { projection ->
            projection.type.asTypeName(typeParams)?.let { listOf(it) } ?: emptyList()
          })
      }
    }
    is KmClassifier.TypeAlias -> {
      ClassName.bestGuess(classifier.name.qualifiedName)
    }
    is KmClassifier.TypeParameter -> {
      typeParams
        .find { it.id == classifier.id }?.let { paramType ->
          TypeVariableName(paramType.name)
        }
    }
  }
}
