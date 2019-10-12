package com.github.kvnxiao.discord

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

    val FIRST_8_DIGITS = listOf(
        DIGIT_1,
        DIGIT_2,
        DIGIT_3,
        DIGIT_4,
        DIGIT_5,
        DIGIT_6,
        DIGIT_7,
        DIGIT_8
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
            else -> -1
        }
    }
}
