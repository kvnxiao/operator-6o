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

import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class ArgumentsSpec : Spek({
    val emptyArguments = Arguments("", null)

    describe("An alias/argument pair") {
        context("input string is null") {
            it("null should return default arguments") {
                Arguments.from(null) shouldEqual emptyArguments
            }
        }

        context("input string is empty/blank") {
            it("empty should return default arguments") {
                Arguments.from("") shouldEqual emptyArguments
            }
            it("blank should return default arguments") {
                Arguments.from("  \t   ") shouldEqual emptyArguments
            }
        }

        context("input string is single word") {
            it("should return with null arguments") {
                val (alias, arguments) = Arguments.from("test")
                alias shouldEqual "test"
                arguments.shouldBeNull()
            }
        }

        context("input string is multi-word") {
            it("should return with non-null arguments") {
                val (alias, arguments) = Arguments.from("test 123 abc")
                alias shouldEqual "test"
                arguments.shouldNotBeNull()
                arguments shouldEqual "123 abc"
            }

            it("should be able to call next() and return non-null arguments") {
                val arg1 = Arguments.from("test 123 abc")
                arg1 shouldEqual Arguments("test", "123 abc")
                val arg2 = arg1.next()
                arg2 shouldEqual Arguments("123", "abc")
                val arg3 = arg2.next()
                arg3 shouldEqual Arguments("abc", null)
                arg3.next() shouldEqual emptyArguments
            }
        }
    }
})
