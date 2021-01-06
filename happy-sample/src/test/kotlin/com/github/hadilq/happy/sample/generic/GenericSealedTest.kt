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
package com.github.hadilq.happy.sample.generic

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test

class GenericSealedTest {

  @Test
  fun happyPathTest() {
    val result: A.HappyA<Int> = happy() elseIf {
      OptionOne {
        assert(false)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
      OptionThree { _, _, _ ->
        assert(false)
        return
      }
      OptionFour {
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionOneTest() {
    val result: A.HappyA<Int> = optionOne() elseIf {
      OptionOne {
        assert(true)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
      OptionThree { _, _, _ ->
        assert(false)
        return
      }
      OptionFour {
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionTwoTest() {
    val result: A.HappyA<Int> = optionTwo() elseIf {
      OptionOne {
        assert(false)
        return
      }
      OptionTwo { _, _, _ ->
        assert(true)
        return
      }
      OptionThree { _, _, _ ->
        assert(false)
        return
      }
      OptionFour {
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionThreeTest() {
    val result: A.HappyA<Int> = optionThree() elseIf {
      OptionOne {
        assert(true)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
      OptionThree { _, _, _ ->
        assert(true)
        return
      }
      OptionFour {
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionFourTest() {
    val result: A.HappyA<Int> = optionFour() elseIf {
      OptionOne {
        assert(false)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
      OptionThree { _, _, _ ->
        assert(false)
        return
      }
      OptionFour {
        assert(true)
        return
      }
    }
  }

  private fun happy(): A<Int> = A.HappyA(1)
  private fun optionOne(): A<Int> = A.OptionOne()
  private fun optionTwo(): A<Int> = A.OptionTwo(1, listOf(2), 3)
  private fun optionThree(): A<Int> = A.OptionThree(listOf(""), mapOf(), listOf())
  private fun optionFour(): A<Int> = A.OptionFour(mapOf())
}

sealed class A<T> {
  @Happy
  data class HappyA<T>(val a: T) : A<T>()
  class OptionOne<T> : A<T>()
  data class OptionTwo<T>(val one: T, val two: List<T>, val three: Int) : A<T>()
  data class OptionThree<T>(val one: List<String>, val two: Alias, val three: List<String>) : A<T>()
  data class OptionFour<T>(val one: Map<String, List<Int>>) : A<T>()
}

typealias Alias = Map<String, String>
