package com.github.kvnxiao.discord.guild.audio.reaction

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Member

data class AudioSearchSelection(
    val member: Member,
    val tracks: List<AudioTrack>
)
