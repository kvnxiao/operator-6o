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
import com.github.kvnxiao.discord.guild.audio.GuildAudioRegistry
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.spec.VoiceChannelJoinSpec
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("join")
@Descriptor(
    description = "Makes the bot join the voice channel that the calling user is in.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = false)
class JoinCommand(
    private val audioRegistry: GuildAudioRegistry
) : GuildCommand() {
    override fun execute(ctx: Context, guild: Guild): Mono<Void> =
        audioRegistry.getOrCreateFirst(guild.id.asLong()).flatMap { audioManager ->
            ctx.event.message.authorAsMember
                .flatMap(Member::getVoiceState)
                .flatMap { voiceState ->
                    voiceState.channel.flatMap { voiceChannel ->
                        voiceChannel.join(VoiceChannelJoinSpec.builder().provider(audioManager.provider).build())
                    }
                }
                .then()
        }
}
