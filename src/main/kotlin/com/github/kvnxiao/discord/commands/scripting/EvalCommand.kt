package com.github.kvnxiao.discord.commands.scripting

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import discord4j.core.`object`.reaction.ReactionEmoji
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
    private val graalContext: org.graalvm.polyglot.Context
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
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
