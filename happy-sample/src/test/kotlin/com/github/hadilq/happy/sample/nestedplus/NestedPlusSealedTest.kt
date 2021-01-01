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
package com.github.hadilq.happy.sample.nestedplus

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test

class NestedPlusSealedTest {

  @Test
  fun optionOneTest() {
    val result: A.B.HappyA = optionOne() elseIf {
      BOptionOne {
        assert(true)
        return
      }
      BOptionTwo { _, _, _ ->
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionTwoTest() {
    val result: A.B.HappyA = optionTwo() elseIf {
      BOptionOne {
        assert(false)
        return
      }
      BOptionTwo { _, _, _ ->
        assert(true)
        return
      }
    }
  }

  private fun optionOne(): A = A.B.OptionOne

  private fun optionTwo(): A = A.B.OptionTwo(1, 2, 3)
}

sealed class A {
  abstract class B : A() {
    @Happy
    data class HappyA(val a: Int) : B()
    object OptionOne : B()
    data class OptionTwo(val one: Int, val two: Int, val three: Int) : B()
  }

  abstract class C : A() {
    data class OptionThree(val one: Int, val two: Int) : C()
    object OptionFour : C()
  }
}
