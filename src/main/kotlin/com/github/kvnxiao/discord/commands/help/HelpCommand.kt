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
package com.github.kvnxiao.discord.commands.help

import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import com.github.kvnxiao.discord.d4j.botMention
import com.github.kvnxiao.discord.d4j.embed
import discord4j.core.`object`.entity.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import com.github.kvnxiao.discord.command.descriptor.Descriptor as DescriptorString

@Component
@Id("help")
@Alias(["help", "man"])
@Descriptor(
    description = "Displays the manual for a provided command alias.",
    usage = "%A <command alias without prefix>"
)
@Permissions(allowDirectMessaging = true)
class HelpCommand(
    private val prefixSettings: PrefixSettings,
    private val propertiesRegistry: PropertiesRegistry
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        Mono.just(prefixSettings.getPrefixOrDefault(ctx.guild?.id))
            .flatMap { prefix ->
                commandUsage(ctx, prefix, propertiesRegistry)
                    .switchIfEmpty(defaultMessage(ctx, prefix, propertiesRegistry))
            }
            .then()

    private fun defaultMessage(
        ctx: Context,
        prefix: String,
        propertiesRegistry: PropertiesRegistry
    ): Mono<Message> =
        ctx.channel.createEmbed(
            embed {
                val commandPath = "$prefix${ctx.args.alias}"
                setTitle("Command Manual")
                setDescription("Welcome to the help manual!")
                addField(
                    "How to use",
                    "To see a command's usage, type\n`${formatUsage(
                        ctx.descriptor,
                        commandPath
                    )}`\ne.g. `$commandPath ping` will show information about the ping command.",
                    false
                )
                propertiesRegistry.getTopLevelPropertyById("all")?.let { props ->
                    val aliases = props.aliases.joinToString(separator = " or ") { "`$prefix$it`" }
                    addField(
                        "See all available top-level commands",
                        "To view all available commands, type\n$aliases",
                        false
                    )
                }
            }
        )

    private fun commandUsage(
        ctx: Context,
        prefix: String,
        propertiesRegistry: PropertiesRegistry
    ): Mono<Message> =
        Mono.justOrEmpty(propertiesRegistry.getPropertiesFromAlias(ctx.args.next()))
            .flatMap { (props, subAliases, pathList) ->
                val mention = ctx.event.client.botMention()
                ctx.channel.createEmbed(
                    embed {
                        val fullCommandPath = pathList.joinToString(separator = " ")
                        val replacement = if (props.permissions.requireBotMention) {
                            "$mention `$fullCommandPath`"
                        } else {
                            "`$prefix$fullCommandPath`"
                        }

                        setTitle("Command Manual")
                        addField("ID", props.id, true)
                        addField("Aliases", props.aliases.joinToString(), true)
                        addField("Permissions Required", props.formatPermissions(), false)
                        addField("Description", props.descriptor.description, false)
                        addField("Usage", formatUsage(props.descriptor, replacement), false)
                        addField("Sub-commands", formatSubCommands(subAliases, replacement), false)
                    }
                )
            }

    private fun formatUsage(descriptor: DescriptorString, replacement: String): String =
        descriptor.usage.replace("%A", replacement)

    private fun formatSubCommands(subAliases: List<String>, replacement: String): String =
        if (subAliases.isEmpty()) "N/A" else subAliases.joinToString(separator = "\n") {
            "$it: $replacement `$it` `...`"
        }

    private fun CommandProperties.formatPermissions(): String {
        return this.permissions.let { perms ->
            StringBuilder().apply {
                append(perms.permSet.asEnumSet().toString())
                if (perms.requireGuildOwner) append("\nrequireGuildOwner")
                if (perms.requireBotOwner) append("\nrequireBotOwner")
                if (perms.requireBotMention) append("\nrequireBotMention")
                if (perms.requireDirectMessaging) append("\nrequireDirectMessaging")
            }.toString()
        }
    }
}
