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
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Client(
    private val commandProcessor: CommandProcessor,
    annotationProcessor: AnnotationProcessor,
    rootRegistry: RegistryNode,
    commands: List<Command>,
    @Value(Environment.TOKEN) private val token: String
) : ApplicationRunner {

    private val client: DiscordClient = DiscordClientBuilder(token).build()

    init {
        annotationProcessor.process(commands, rootRegistry)

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

    override fun run(args: ApplicationArguments) {
        client.login().subscribe()
    }
}
