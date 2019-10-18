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
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("next")
@Descriptor(
    description = "Plays the next audio track if one exists.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = false)
class NextCommand(
    private val guildAudioState: GuildAudioState
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null) Mono.empty()
        else Mono.just(guildAudioState.getOrCreateForGuild(ctx.guild.id))
            .filter { audioManager -> audioManager.voiceConnectionManager.isVoiceConnected() }
            .doOnNext { audioManager -> audioManager.next() }
            .then()
}
