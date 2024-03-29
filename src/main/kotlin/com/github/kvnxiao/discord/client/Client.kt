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
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.shard.GatewayBootstrap
import discord4j.core.shard.ShardingStrategy
import discord4j.gateway.GatewayOptions
import discord4j.rest.request.RouteMatcher
import discord4j.rest.response.ResponseFunction
import io.netty.channel.unix.Errors.NativeIoException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.retry.Retry

private val logger: KLogger = KotlinLogging.logger {}

@Component
class Client(
    commandProcessor: CommandProcessor,
    annotationProcessor: AnnotationProcessor,
    rootRegistry: RegistryNode,
    commands: List<Command>,
    @Value(Environment.TOKEN) private val token: String
) : DisposableBean {

    private val gatewayBootstrap: GatewayBootstrap<GatewayOptions> =
        DiscordClient.builder(token)
            .onClientResponse(
                ResponseFunction.retryWhen(
                    RouteMatcher.any(),
                    Retry.anyOf<NativeIoException>(NativeIoException::class.java)
                        .retryMax(5)
                        .doOnRetry { logger.warn { "Encountered ${NativeIoException::class.simpleName}, retrying..." } }
                )
            )
            .build()
            .gateway()
            .setSharding(ShardingStrategy.recommended())
    private val gatewayClient: GatewayDiscordClient

    init {
        annotationProcessor.process(commands, rootRegistry)

        gatewayClient = gatewayBootstrap.withEventDispatcher { ed ->
            ed.on(ReadyEvent::class.java)
                .next()
                .info(logger) { event -> "Logged in as ${event.self.username}#${event.self.discriminator}" }
                .map { readyEvent -> readyEvent.guilds.size }
                .flatMap { size ->
                    ed.on(GuildCreateEvent::class.java)
                        .take(size.toLong())
                        .map { it.guild.id }
                        .collectList()
                        .flatMap { commandProcessor.loadPrefixSettings(it) }
                        .info(logger) { "Done loading $size guilds' settings." }
                }
                .info(logger) { "Ready to receive commands." }
                .thenMany(
                    ed.on(MessageCreateEvent::class.java)
                        .flatMap(commandProcessor::processMessageCreateEvent)
                )
        }.login().block() ?: throw RuntimeException("Failed to connect to the Discord gateway.")
    }

    override fun destroy() {
        logger.info { "Shutting down..." }
        gatewayClient.logout().block()
    }
}
