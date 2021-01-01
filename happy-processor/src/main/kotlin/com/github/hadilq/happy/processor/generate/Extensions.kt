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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
public val ImmutableKmClass.className: ClassName
  get() = with(name) { ClassName(packageName, *simpleNames.toTypedArray()) }

 public val String.qualifiedName: String
  get() = replace("/", ".")

 public val String.packageName: String
  get() = substringBeforeLast("/").replace("/", ".")

public val String.simpleNames: List<String>
  get() = substringAfterLast("/").split(".")

public val String.simpleName: String
  get() = qualifiedName.substringAfterLast(".")

@KotlinPoetMetadataPreview
public val ImmutableKmClass.qualifiedName: String
  get() = name.qualifiedName

@KotlinPoetMetadataPreview
public val ImmutableKmClass.simpleNames: List<String>
  get() = name.simpleNames

@KotlinPoetMetadataPreview
public val ImmutableKmClass.simpleName: String
  get() = name.simpleName
