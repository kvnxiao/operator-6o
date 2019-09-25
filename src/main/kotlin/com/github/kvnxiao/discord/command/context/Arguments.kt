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
package com.github.kvnxiao.discord.command.context

import com.github.kvnxiao.discord.command.Alias
import java.util.regex.Pattern

/**
 * A data class representing a potential pair of command alias and arguments.
 */
data class Arguments(val alias: Alias, val arguments: String?) {
    companion object {
        private val SPLIT_REGEX: Pattern = Pattern.compile("\\s+|\\n+")
        val EMPTY: Arguments = Arguments("", null)

        /**
         * Process a single text input string into an arguments pair.
         * The string input is split by whitespace (including new-line whitespace).
         *
         * For example:
         *   - "test abc 123" is processed into -> alias="test", arguments="abc 123"
         *   - "test\nwith new-lines" is processed into -> alias="test", arguments="with new-lines"
         *
         * If the input string is null or blank, a default value is set for the alias-arguments pair: (alias="",
         * arguments=null)
         */
        @JvmStatic
        fun from(input: String?): Arguments =
            if (input.isNullOrBlank()) {
                EMPTY
            } else {
                val split = SPLIT_REGEX.split(input, 2)
                Arguments(split[0], if (split.size == 1) null else split[1])
            }
    }

    /**
     * Attempts to further process the arguments value of the current Arguments instance. If the current arguments value
     * is null or blank, returns the default value for the alias-arguments pair: (alias="", arguments=null)
     */
    fun next(): Arguments = from(arguments)
}
