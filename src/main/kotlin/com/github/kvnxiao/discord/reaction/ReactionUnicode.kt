/**
 *   Copyright 2019 Ze Hao (Kevin) Xiao
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.kvnxiao.discord.reaction

object ReactionUnicode {
    const val ARROW_FORWARD = "\u25b6"
    const val EIGHTBALL = "\uD83C\uDFB1"
    const val MAG_RIGHT = "\uD83D\uDD0E"
    const val DIGIT_0 = "\u0030\u20E3"
    const val DIGIT_1 = "\u0031\u20E3"
    const val DIGIT_2 = "\u0032\u20E3"
    const val DIGIT_3 = "\u0033\u20E3"
    const val DIGIT_4 = "\u0034\u20E3"
    const val DIGIT_5 = "\u0035\u20E3"
    const val DIGIT_6 = "\u0036\u20E3"
    const val DIGIT_7 = "\u0037\u20E3"
    const val DIGIT_8 = "\u0038\u20E3"
    const val DIGIT_9 = "\u0039\u20E3"
    const val DIGIT_10 = "\uD83D\uDD1F"
    const val TIMER = "\u23F2"
    const val CHECKMARK = "\u2714"
    const val CROSSMARK = "\u274C"

    val DIGITS_FROM_1 = listOf(
        DIGIT_1,
        DIGIT_2,
        DIGIT_3,
        DIGIT_4,
        DIGIT_5,
        DIGIT_6,
        DIGIT_7,
        DIGIT_8,
        DIGIT_9,
        DIGIT_10
    )

    fun getIndexFromDigits(digit: String): Int {
        return when (digit) {
            DIGIT_1 -> 0
            DIGIT_2 -> 1
            DIGIT_3 -> 2
            DIGIT_4 -> 3
            DIGIT_5 -> 4
            DIGIT_6 -> 5
            DIGIT_7 -> 6
            DIGIT_8 -> 7
            DIGIT_9 -> 8
            DIGIT_10 -> 9
            else -> -1
        }
    }
}
