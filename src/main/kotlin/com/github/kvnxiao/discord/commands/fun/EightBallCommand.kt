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
package com.github.kvnxiao.discord.commands.`fun`

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import java.util.concurrent.ThreadLocalRandom
import reactor.core.publisher.Mono

@Id("8ball")
@Descriptor(
    description = "The magic 8-ball sees all.",
    usage = "%A <question>"
)
class EightBallCommand : CommandExecutable {
    companion object {
        private const val EIGHTBALL_EMOJI = "\uD83C\uDFB1"
        private val LINES = listOf(
            "It is certain",
            "It is decidedly so",
            "Without a doubt",
            "Yes, definitely",
            "You may rely on it",
            "As I see it, yes",
            "Most likely",
            "Outlook good",
            "Yes",
            "Signs point to yes",
            "Reply hazy try again",
            "Ask again later",
            "Better not tell you now",
            "Cannot predict now",
            "Concentrate and ask again",
            "Don't count on it",
            "My reply is no",
            "My sources say no",
            "Outlook not so good",
            "Very doubtful"
        )
    }

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .flatMap { ctx.channel.createMessage("**Question:** $it\n$EIGHTBALL_EMOJI**: ${randomAnswer()}**") }
            .then()

    private fun randomAnswer(): String = LINES[ThreadLocalRandom.current().nextInt(LINES.size)]
}
