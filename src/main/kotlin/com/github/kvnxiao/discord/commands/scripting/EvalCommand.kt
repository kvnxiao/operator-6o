/**
 *   Copyright 2020 Ze Hao (Kevin) Xiao
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
package com.github.kvnxiao.discord.commands.scripting

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.d4j.message
import org.graalvm.polyglot.Context
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import com.github.kvnxiao.discord.command.context.Context as CommandContext

@Component
@Id("eval")
@Descriptor(
    description = "Evaluates JavaScript source code and prints the result.",
    usage = "%A <source code>"
)
@Permissions(allowDirectMessaging = true)
class EvalCommand(
    private val graalContext: Context
) : Command {

    private fun stripCodeblocks(s: String): String = s.trim().let {
        when {
            it.startsWith("```") && it.endsWith("```") -> it.substring(3, it.length - 3).trim()
            it.startsWith("`") && it.endsWith("`") -> it.substring(1, it.length - 1).trim()
            else -> it
        }
    }

    override fun execute(ctx: CommandContext): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .map(::stripCodeblocks)
            .flatMap { strippedArgs ->
                Mono.just(graalContext.eval("js", strippedArgs))
                    .flatMap { value ->
                        ctx.channel.createMessage(
                            message {
                                messageReference(ctx.event.message.id)
                                content("```\n${value}\n```")
                            }
                        )
                    }
            }
            .onErrorResume {
                ctx.channel.createMessage(
                    message {
                        messageReference(ctx.event.message.id)
                        content("```\n${it.message}\n```")
                    }
                )
            }
            .then()
}
