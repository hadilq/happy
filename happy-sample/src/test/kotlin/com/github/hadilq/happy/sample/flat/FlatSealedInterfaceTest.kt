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
package com.github.hadilq.happy.sample.flat

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test

class FlatSealedInterfaceTest {

  @Test
  fun happyTest() {
    val result: HappyAI = happy() elseIf {
      OptionOneI {
        assert(false)
        return
      }
      OptionTwoI { _, _, _ ->
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionOneTest() {
    val result: HappyAI = optionOne() elseIf {
      OptionOneI {
        assert(true)
        return
      }
      OptionTwoI { _, _, _ ->
        assert(false)
        return
      }
    }
  }

  @Test
  fun optionTwoTest() {
    val result: HappyAI = optionTwo() elseIf {
      OptionOneI {
        assert(false)
        return
      }
      OptionTwoI { _, _, _ ->
        assert(true)
        return
      }
    }
  }

  private fun happy():AI = HappyAI(1)

  private fun optionOne(): AI = OptionOneI

  private fun optionTwo(): AI = OptionTwoI(1, 2, 3)
}

sealed interface AI

@Happy
data class HappyAI(val a: Int) : AI
object OptionOneI : AI
data class OptionTwoI(val one: Int, val two: Int, val three: Int) : AI
