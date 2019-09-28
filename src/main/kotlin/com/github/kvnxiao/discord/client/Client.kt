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
package com.github.kvnxiao.discord.client

import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.DiscordCommand
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.descriptor.Descriptor
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import com.github.kvnxiao.discord.command.permission.Permissions
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.ratelimit.RateLimits
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.env.Environment
import com.github.kvnxiao.discord.koin.getAll
import com.github.kvnxiao.discord.koin.getProperty
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get

private val logger = KotlinLogging.logger {}

class Client : KoinComponent {
    private val commandProcessor: CommandProcessor = get()
    private val rootRegistry: RegistryNode = get()

    private val token: String = getProperty(Environment.TOKEN)

    private val client: DiscordClient = DiscordClientBuilder(token).build()

    init {
        registerCommands()

        client.eventDispatcher.apply {
            on(ReadyEvent::class.java)
                .info(logger) { event -> "Logged in as ${event.self.username}#${event.self.discriminator}" }
                .map { event -> event.guilds.size }
                .flatMap { size ->
                    this.on(GuildCreateEvent::class.java)
                        .take(size.toLong())
                        .last()
                        .doOnNext { commandProcessor.loadPrefixSettings() }
                        .info(logger) { "Done loading $size guilds." }
                }
                .info(logger) { "Ready to receive commands." }
                .flatMap {
                    this.on(MessageCreateEvent::class.java)
                        .flatMap(commandProcessor::processMessageCreateEvent)
                }
                .subscribe()
        }
    }

    fun run() {
        client.login().block()
    }

    private fun registerCommands() {
        val executables: List<CommandExecutable> = getAll()
        executables.forEach { ex ->
            val annotations = ex::class.annotations
            val id = (annotations.find { it is Id } as Id).id
            val aliases = (annotations.find { it is Alias } as? Alias)?.aliases?.toSet() ?: setOf(id)
            val descriptor =
                (annotations.find { it is com.github.kvnxiao.discord.command.annotation.Descriptor }
                        as? com.github.kvnxiao.discord.command.annotation.Descriptor)?.let {
                    Descriptor(it.description, it.usage)
                } ?: Descriptor()
            val rateLimits =
                (annotations.find { it is com.github.kvnxiao.discord.command.annotation.RateLimits }
                        as? com.github.kvnxiao.discord.command.annotation.RateLimits)?.let {
                    RateLimits(it.rateLimitOnGuild, it.tokensPerPeriod, it.rateLimitPeriodMs)
                } ?: RateLimits()

            val permissions =
                (annotations.find { it is com.github.kvnxiao.discord.command.annotation.Permissions }
                        as? com.github.kvnxiao.discord.command.annotation.Permissions)?.let {
                    Permissions(
                        it.requireBotOwner,
                        it.requireGuildOwner,
                        it.requireBotMention,
                        it.allowDirectMessaging,
                        it.requireDirectMessaging,
                        it.removeInvocationMessage,
                        PermissionSet.of(*it.permSet)
                    )
                } ?: Permissions()

            val command = DiscordCommand(
                CommandProperties(
                    id,
                    aliases,
                    descriptor,
                    rateLimits,
                    permissions
                ),
                ex
            )
            rootRegistry.register(command)
        }
    }
}
