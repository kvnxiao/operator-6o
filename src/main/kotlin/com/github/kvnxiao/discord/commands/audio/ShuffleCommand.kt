package com.github.kvnxiao.discord.commands.audio

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.GuildCommand
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import discord4j.core.`object`.entity.Guild
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("shuffle")
@Descriptor(
    description = "Shuffles all enqueued audio tracks.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = false)
class ShuffleCommand(
    private val guildAudioState: GuildAudioState
) : GuildCommand() {
    override fun execute(ctx: Context, guild: Guild): Mono<Void> =
        Mono.just(guildAudioState.getOrCreateForGuild(guild.id))
            .filter { audioManager -> audioManager.voiceConnectionManager.isVoiceConnected() }
            .doOnNext { audioManager -> audioManager.shuffle() }
            .then()
}
