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
package com.github.kvnxiao.discord.command.processor

import com.github.kvnxiao.discord.command.DiscordCommand
import com.github.kvnxiao.discord.command.context.Arguments
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.isMention
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.validation.context.ContextValidator
import com.github.kvnxiao.discord.command.validation.message.MessageValidator
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

/**
 * Command processing handler which consumes [MessageCreateEvent]s from Discord, parses the message contents into
 * arguments, and attempts to retrieve and execute commands that match the arguments if they exist.
 */
class CommandProcessor(
    messageValidators: List<MessageValidator>,
    contextValidators: List<ContextValidator>,
    private val rootRegistry: RegistryNode,
    private val prefixSettings: PrefixSettings
) {

    private val messageValidators: Flux<MessageValidator> = Flux.fromIterable(messageValidators)
    private val contextValidators: Flux<ContextValidator> = Flux.fromIterable(contextValidators)

    /**
     * Loads custom alias prefixes for each guild.
     */
    fun loadPrefixSettings() = prefixSettings.loadGuildPrefixes()

    /**
     * Validates and processes [MessageCreateEvent]s into potential commands, creating a context and executing the
     * command with said context upon successful validations.
     */
    fun processMessageCreateEvent(event: MessageCreateEvent): Mono<Void> = Mono.just(event)
        .filterWhen(this::validateMessageCreateEvent)
        .flatMap(this::getCommandWithContext)
        .filter { (command, context) -> command.rateLimiter.isNotRateLimited(context.guild, context.user) }
        .flatMap { (command, context) -> execute(command, context) }

    private fun validateMessageCreateEvent(event: MessageCreateEvent): Mono<Boolean> =
        messageValidators
            .flatMap { validator -> validator.validate(event.message) }
            .all { result -> result }

    private fun validateContext(context: Context): Mono<Boolean> =
        contextValidators
            .flatMap { validator -> validator.validate(context) }
            .all { result -> result }

    private fun execute(command: DiscordCommand, context: Context): Mono<Void> =
        command.executable.execute(context)

    private fun getCommandWithContext(event: MessageCreateEvent): Mono<Tuple2<DiscordCommand, Context>> {
        val prefix = event.guildId.map { prefixSettings.getPrefixOrDefault(it) }
            .orElse(PrefixSettings.DEFAULT_PREFIX)
        return Mono.justOrEmpty(event.message.content)
            .filter { content -> content.startsWith(prefix) }
            .map { content -> content.substring(prefix.length) }
            .flatMap { strippedContent ->
                val initialArgs = Arguments.from(strippedContent)
                val wasBotMentioned: Boolean =
                    event.message.client.selfId.map { initialArgs.alias.isMention(it.asString()) }.orElse(false)
                val arguments: Arguments = if (wasBotMentioned) initialArgs.next() else initialArgs

                getCommandFromAlias(arguments)
                    .flatMap { (command, args) ->
                        Mono.just(command).zipWith(createContext(event, command, args, wasBotMentioned))
                    }
            }
    }

    private fun createContext(
        event: MessageCreateEvent,
        command: DiscordCommand,
        args: Arguments,
        wasBotMentioned: Boolean
    ): Mono<Context> =
        Mono.zip(event.message.channel, event.client.applicationInfo)
            .flatMap { (channel, appInfo) ->
                val user = event.message.author.get()
                val isBotOwner = appInfo.ownerId == user.id
                val isDirectMessage = channel !is GuildChannel

                // Attempt to create a command context with non-null guild
                event.guild.map { guild ->
                    Context(
                        event,
                        channel,
                        guild,
                        user,
                        args,
                        command.properties.id,
                        args.alias,
                        command.properties.descriptor,
                        command.properties.permissions,
                        command.properties.rateLimits,
                        isBotOwner,
                        isDirectMessage,
                        wasBotMentioned
                    )
                }.switchIfEmpty(
                    Mono.just(
                        Context(
                            event,
                            channel,
                            null,
                            user,
                            args,
                            command.properties.id,
                            args.alias,
                            command.properties.descriptor,
                            command.properties.permissions,
                            command.properties.rateLimits,
                            isBotOwner,
                            isDirectMessage,
                            wasBotMentioned
                        )
                    )
                )
            }
            .filterWhen(this::validateContext)

    internal fun getCommandFromAlias(args: Arguments): Mono<Tuple2<DiscordCommand, Arguments>> {
        var currArgs: Arguments = args
        var prevArgs: Arguments = args
        var currNode: RegistryNode? = rootRegistry
        var currCommand: DiscordCommand? = null
        while (currNode != null && currArgs.alias.isNotEmpty() && currNode.subNodeFromAlias(currArgs.alias) != null) {
            currCommand = currNode.commandFromAlias(currArgs.alias)
            currNode = currNode.subNodeFromAlias(currArgs.alias)
            prevArgs = currArgs
            currArgs = currArgs.next()
        }
        return if (currCommand == null) Mono.empty() else Mono.just(Tuples.of(currCommand, prevArgs))
    }
}
