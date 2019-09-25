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
package com.github.kvnxiao.discord.command.processor

import com.github.kvnxiao.discord.command.command
import com.github.kvnxiao.discord.command.context.Arguments
import com.github.kvnxiao.discord.command.registry.MapTreeRegistryRoot
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.verifyInput
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import reactor.util.function.Tuples

class CommandProcessorSpec : Spek({
    val test1 = command("test1")
    val d2 = command("d2")
    val d3 = command("d3")
    val c123 = command("123")
    val c456 = command("456")
    val abc = command("abc")
    val registry: RegistryNode = MapTreeRegistryRoot().apply {
        this.register(test1).register(d2).register(d3)
        this.register(c123).register(c456)
        this.register(abc)
    }
    val processor = CommandProcessor(listOf(), listOf(), registry)

    describe("Command registry with commands (test1->d1->d2, 123->456, abc)") {
        describe("testing for commands test1->d1->d2") {
            context("input string is 'test1'") {
                it("should return command with id=test1") {
                    processor.verifyInput("test1")
                        .expectNext(Tuples.of(test1, Arguments("test1", null)))
                        .verifyComplete()
                }
            }
            context("input string is 'test1 abc'") {
                it("should return command with id=test1 and args=abc") {
                    processor.verifyInput("test1 abc")
                        .expectNext(Tuples.of(test1, Arguments("test1", "abc")))
                        .verifyComplete()
                }
            }

            context("input string is 'test1 d2'") {
                it("should return sub-command of test1 with id=d2") {
                    processor.verifyInput("test1 d2")
                        .expectNext(Tuples.of(d2, Arguments("d2", null)))
                        .verifyComplete()
                }
            }
            context("input string is 'test1 d2 extra arguments go here'") {
                it("should return sub-command of test1 with id=d2 and args=extra arguments go here") {
                    processor.verifyInput("test1 d2 extra arguments go here")
                        .expectNext(Tuples.of(d2, Arguments("d2", "extra arguments go here")))
                        .verifyComplete()
                }
            }

            context("input string is 'test1 d2 d3'") {
                it("should return sub-command of test1->d2 with id=d3") {
                    processor.verifyInput("test1 d2 d3")
                        .expectNext(Tuples.of(d3, Arguments("d3", null)))
                        .verifyComplete()
                }
            }
            context("input string is 'test1 d2 d3 \\n args on new-line'") {
                it("should return sub-command of test1->d2 with id=d3 and args=args on new-line") {
                    processor.verifyInput("test1 d2 d3 \n args on new-line")
                        .expectNext(Tuples.of(d3, Arguments("d3", "args on new-line")))
                        .verifyComplete()
                }
            }
        }

        describe("testing for commands 123->456") {
            context("input string is 123") {
                it("should return command with id=123") {
                    processor.verifyInput("123")
                        .expectNext(Tuples.of(c123, Arguments("123", null)))
                        .verifyComplete()
                }
            }

            context("input string is 123 456") {
                it("should return sub-command of 123 with id=456") {
                    processor.verifyInput("123 456")
                        .expectNext(Tuples.of(c456, Arguments("456", null)))
                        .verifyComplete()
                }
            }
        }

        describe("testing for single root command abc") {
            context("input string is abc") {
                it("should return command with id=abc") {
                    processor.verifyInput("abc")
                        .expectNext(Tuples.of(abc, Arguments("abc", null)))
                        .verifyComplete()
                }
            }
        }

        describe("testing for non-existent command ids") {
            context("input string is null") {
                it("should return no command") {
                    processor.verifyInput(null)
                        .verifyComplete()
                }
            }
            context("input string is empty ''") {
                it("should return no command") {
                    processor.verifyInput("")
                        .verifyComplete()
                }
            }
            context("input string is 'xyz'") {
                it("should return no command") {
                    processor.verifyInput("xyz")
                        .verifyComplete()
                }
            }
        }
    }
})
