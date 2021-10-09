package com.github.hadilq.happy.processor.generate

import com.github.hadilq.happy.processor.HType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
public fun generateElvisFunction(
  sealedParentHType: HType,
  happyHType: HType,
  cases: List<Pair<List<String>, HType>>
): FunSpec.Builder {
  val elvisFunBuilder = FunSpec.builder(ELVIS)
    .addModifiers(KModifier.PUBLIC)
    .addModifiers(KModifier.INLINE)
    .addTypeVariables(sealedParentHType.typeParameters)
    .receiver(sealedParentHType.className)
    .returns(happyHType.className)
    .addParameters(
      cases.map { (names, caseClass) ->
        ParameterSpec.builder(
          names.joinToString(""),
          LambdaTypeName.get(
            parameters = listOf(ParameterSpec.builder(BLOCK_NAME, caseClass.className).build()),
            returnType = happyHType.className
          )
        ).build()
      }.asIterable()
    ).beginControlFlow("return when(this)")

  elvisFunBuilder.addStatement("is %T -> this", happyHType.className)

  cases.forEach { (names, caseClass) ->
    elvisFunBuilder.addStatement("is %T -> ${names.joinToString("")}(this)", caseClass.className)
  }
  elvisFunBuilder.endControlFlow()

  if (cases.size == 1) {
    elvisFunBuilder.addModifiers(KModifier.INFIX)
  }
  return elvisFunBuilder
}
