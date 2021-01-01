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
package com.github.hadilq.happy.sample.nested

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test

class NestedTest {

  @Test
  fun happyPathTest() {
    val result: A.HappyA = happy() elseIf {
      OptionOne {
        assert(false)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionOneTest() {
    val result: A.HappyA = optionOne() elseIf {
      OptionOne {
        assert(true)
        return
      }
      OptionTwo { _, _, _ ->
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionTwoTest() {
    val result: A.HappyA = optionTwo() elseIf {
      OptionOne {
        assert(false)
        return
      }
      OptionTwo { _, _, _ ->
        assert(true)
        return
      }
    }
  }

  private fun happy():A = A.HappyA(1)

  private fun optionOne(): A = A.OptionOne

  private fun optionTwo(): A = A.OptionTwo(1, 2, 3)
}

sealed class A {
  @Happy
  data class HappyA(val a: Int) : A()
  object OptionOne : A()
  data class OptionTwo(val one: Int, val two: Int, val three: Int) : A()
}
