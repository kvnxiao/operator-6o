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
package com.github.kvnxiao.discord.commands.system

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import java.time.Duration
import java.time.Instant
import reactor.core.publisher.Mono

@Id("uptime")
@Descriptor(
    description = "Shows how long the bot has been running for.",
    usage = "%A"
)
class UptimeCommand : CommandExecutable {

    private val startTime: Instant = Instant.now()

    override fun execute(ctx: Context): Mono<Void> =
        ctx.channel.createMessage(
            "System uptime: ${Duration.between(startTime, Instant.now()).formatDuration()}"
        ).then()

    private fun Duration.formatDuration(): String =
        kotlin.math.abs(this.seconds)
            .let { "**${it / 86400}**d **${(it % 86400) / 3600}**h **${(it % 3600) / 60}**min **${it % 60}**s" }
}
