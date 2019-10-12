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

import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.processor.AnnotationProcessor
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.env.Environment
import com.github.kvnxiao.discord.koin.getAll
import com.github.kvnxiao.discord.koin.getProperty
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get

private val logger = KotlinLogging.logger {}

class Client : KoinComponent {
    private val commandProcessor: CommandProcessor = get()
    private val annotationProcessor: AnnotationProcessor = get()
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
                    // All guilds have been loaded at this point
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
        val executables: List<Command> = getAll()
        annotationProcessor.process(executables, rootRegistry)
    }
}
