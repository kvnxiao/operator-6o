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
package com.github.kvnxiao.discord.command

import com.github.kvnxiao.discord.command.context.Arguments
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.descriptor.Descriptor
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.permission.Permissions
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.ratelimit.RateLimits
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.function.Tuple2

fun command(id: Id): DiscordCommand = DiscordCommand(
    CommandProperties(id, setOf(id), Descriptor(), RateLimits(), Permissions()),
    object : Command {
        override fun execute(ctx: Context): Mono<Void> = Mono.empty()
    }
)

fun CommandProcessor.verifyInput(input: String?): StepVerifier.FirstStep<Tuple2<DiscordCommand, Arguments>> =
    StepVerifier.create(this.getCommandFromAlias(Arguments.from(input)))
