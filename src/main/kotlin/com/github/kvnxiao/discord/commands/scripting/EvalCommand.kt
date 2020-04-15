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
import com.github.kvnxiao.discord.command.context.Context as CommandContext
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import discord4j.core.`object`.reaction.ReactionEmoji
import org.graalvm.polyglot.Context
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
    override fun execute(ctx: CommandContext): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .map { graalContext.eval("js", it) }
            .flatMap { value ->
                ctx.channel.createMessage(value.toString())
                    .then(ctx.event.message.addReaction(ReactionEmoji.unicode(ReactionUnicode.CHECKMARK)))
            }
            .onErrorResume {
                ctx.channel.createMessage(it.message)
                    .then(ctx.event.message.addReaction(ReactionEmoji.unicode(ReactionUnicode.CROSSMARK)))
            }
}
