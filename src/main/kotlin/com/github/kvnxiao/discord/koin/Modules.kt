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
package com.github.kvnxiao.discord.koin

import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.processor.AnnotationProcessor
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.registry.MapTreeRegistryRoot
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.validation.context.ContextValidator
import com.github.kvnxiao.discord.command.validation.context.MessageContextValidator
import com.github.kvnxiao.discord.command.validation.context.PermissionValidator
import com.github.kvnxiao.discord.command.validation.message.ChannelValidator
import com.github.kvnxiao.discord.command.validation.message.MessageValidator
import com.github.kvnxiao.discord.command.validation.message.SourceValidator
import com.github.kvnxiao.discord.commands.`fun`.EightBallCommand
import com.github.kvnxiao.discord.commands.audio.ClearCommand
import com.github.kvnxiao.discord.commands.audio.JoinCommand
import com.github.kvnxiao.discord.commands.audio.LeaveCommand
import com.github.kvnxiao.discord.commands.audio.NextCommand
import com.github.kvnxiao.discord.commands.audio.NowPlayingCommand
import com.github.kvnxiao.discord.commands.audio.StopCommand
import com.github.kvnxiao.discord.commands.audio.YoutubeCommand
import com.github.kvnxiao.discord.commands.audio.YoutubeSearchCommand
import com.github.kvnxiao.discord.commands.help.AllCommand
import com.github.kvnxiao.discord.commands.help.HelpCommand
import com.github.kvnxiao.discord.commands.search.GoogleCommand
import com.github.kvnxiao.discord.commands.system.PingCommand
import com.github.kvnxiao.discord.commands.system.UptimeCommand
import com.github.kvnxiao.discord.env.Environment
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.github.kvnxiao.discord.guild.audio.reaction.AudioReactionProcessor
import com.github.kvnxiao.discord.guild.audio.reaction.GuildAudioReactionState
import org.koin.core.KoinComponent
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object Modules {
    val validationModule = module {
        single<ContextValidator>(named("context")) { MessageContextValidator() }
        single<ContextValidator>(named("permission")) { PermissionValidator() }
        single<MessageValidator>(named("source")) { SourceValidator() }
        single<MessageValidator>(named("channel")) { ChannelValidator() }
    }
    val commandProcessingModule = module {
        single { PrefixSettings() }
        single<RegistryNode> { MapTreeRegistryRoot() }
        single { PropertiesRegistry(get()) }
        single { CommandProcessor(getAll(), getAll(), get(), get()) }
        single { AnnotationProcessor() }
    }
    val guildModule = module {
        single { GuildAudioState() }
        single { GuildAudioReactionState() }
        single { AudioReactionProcessor(get(), get()) }
    }
    val commandsModule = module {
        // System commands
        command<PingCommand> { PingCommand() }
        command<UptimeCommand> { UptimeCommand() }

        // Help commands
        command<HelpCommand> { HelpCommand(get(), get()) }
        command<AllCommand> { AllCommand(get(), get()) }

        // Query commands
        command<GoogleCommand> {
            GoogleCommand(
                getProperty(Environment.GOOGLE_SEARCH_ENGINE),
                getProperty(Environment.GOOGLE_API_KEY)
            )
        }

        // Fun commands
        command<EightBallCommand> { EightBallCommand() }

        // Audio commands
        command<JoinCommand> { JoinCommand(get()) }
        command<LeaveCommand> { LeaveCommand(get()) }
        command<StopCommand> { StopCommand(get()) }
        command<NowPlayingCommand> { NowPlayingCommand(get()) }
        command<NextCommand> { NextCommand(get()) }
        command<YoutubeCommand> { YoutubeCommand(get()) }
        command<ClearCommand> { ClearCommand(get()) }
        command<YoutubeSearchCommand> { YoutubeSearchCommand(get(), get()) }
    }
}

private inline fun <reified T : Command> Module.command(
    noinline definition: Definition<Command>
) {
    val annotations = T::class.annotations
    val id = annotations.find { an -> an is Id } as? Id
    require(id != null) { "@Id annotation must exist for command ${T::class}" }
    this.single(named(id.id), definition = definition)
}

fun KoinComponent.getProperty(key: String): String = this.getKoin().getProperty<String>(key)
    ?: throw IllegalArgumentException("Missing value for environment variable: $key.")

inline fun <reified T> KoinComponent.getAll(): List<T> = this.getKoin().getAll()
