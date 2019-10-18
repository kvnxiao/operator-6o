package com.github.kvnxiao.discord.commands.`fun`

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.ThreadLocalRandom

@Component
@Id("roll")
@Descriptor(
    description = "Rolls a random number out of the provided bound (inclusive). Defaults to 100 if no bound is given.",
    usage = "%A | %A <bound>"
)
@Permissions(allowDirectMessaging = true)
class RollCommand : Command {

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .map { it.toLong() }
            .onErrorReturn(100L)
            .filter { it > 0L }
            .switchIfEmpty(Mono.just(100L))
            .flatMap { ctx.channel.createMessage(
                ThreadLocalRandom.current().nextLong(it + 1).toString()
            ) }
            .then()
}
