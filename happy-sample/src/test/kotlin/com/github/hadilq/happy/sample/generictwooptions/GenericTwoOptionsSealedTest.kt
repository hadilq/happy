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
package com.github.hadilq.happy.sample.generictwooptions

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test

class GenericTwoOptionsSealedTest {

  @Test
  fun happyPathTest() {
    val result: Success<Int> = success() elseIf {
      assert(false)
      return
    }
  }

  @Test
  fun failureTest() {
    val result: Success<Int> = failure() elseIf {
      assert(true)
      return
    }
  }

  private fun success(): Result<Int> = Success(2)

  private fun failure(): Result<Int> = Failure("")
}

sealed class Result<T>

@Happy
data class Success<T>(val value: T) : Result<T>()
data class Failure<T>(val errorMessage: String) : Result<T>()
