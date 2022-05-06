package com.github.hadilq.happy.processor.common.generate

import com.github.hadilq.happy.annotation.Happy
import com.squareup.kotlinpoet.FileSpec

public sealed class GenerateResult {

  @Happy
  public data class Success(val file: FileSpec): GenerateResult()
}
