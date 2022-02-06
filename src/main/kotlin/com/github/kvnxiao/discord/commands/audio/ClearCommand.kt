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
package com.github.kvnxiao.discord.commands.audio

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.GuildCommand
import com.github.kvnxiao.discord.d4j.embed
import com.github.kvnxiao.discord.guild.audio.GuildAudioRegistry
import discord4j.core.`object`.entity.Guild
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
@Id("clear")
@Descriptor(
    description = "Clears the current audio queue.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = false)
class ClearCommand(
    private val audioRegistry: GuildAudioRegistry
) : GuildCommand() {
    override fun execute(ctx: Context, guild: Guild): Mono<Void> =
        Mono.zip(
            audioRegistry.getOrCreateFirst(guild.id.asLong()),
            ctx.voiceConnections.getVoiceConnection(guild.id)
        )
            .filterWhen { (_, voiceConnection) -> voiceConnection.isConnected }
            .filter { (audioManager) -> audioManager.queueList.isNotEmpty() }
            .doOnNext { (audioManager) -> audioManager.clearQueue() }
            .flatMap {
                ctx.channel.createMessage(
                    embed {
                        initAudioEmbed(0, ctx.user)
                        description("Queue has been cleared!")
                    }
                )
            }
            .then()
}
