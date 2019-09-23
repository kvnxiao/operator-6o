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
 * A data class representing a a potential pair of command alias and arguments, processed from a single text input
 * string. The string input is split by whitespace (including new-line whitespace).
 *
 * For example:
 *   - "test abc 123" is processed into -> alias="test", arguments="abc 123"
 *   - "test\nwith new-lines" is processed into -> alias="test", arguments="with new-lines"
 *
 * If the input string is null or blank, a default value is set for the alias-arguments pair: (alias="", arguments=null)
 */
class Arguments(single: String?) {
    val alias: Alias
    val arguments: String?

    companion object {
        private val SPLIT_REGEX: Pattern = Pattern.compile("\\s+|\\n+")
    }

    init {
        if (single.isNullOrBlank()) {
            this.alias = ""
            this.arguments = null
        } else {
            val split = SPLIT_REGEX.split(single, 2)
            this.alias = split[0]
            this.arguments = if (split.size == 1) null else split[1]
        }
    }

    /**
     * Attempts to further process the arguments value of the current Arguments instance. If the current arguments value
     * is null or blank, returns the default value for the alias-arguments pair: (alias="", arguments=null)
     */
    fun next(): Arguments {
        return Arguments(this.arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arguments

        if (alias != other.alias) return false
        if (arguments != other.arguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alias.hashCode()
        result = 31 * result + (arguments?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Arguments(alias='$alias', arguments=$arguments)"
    }
}
