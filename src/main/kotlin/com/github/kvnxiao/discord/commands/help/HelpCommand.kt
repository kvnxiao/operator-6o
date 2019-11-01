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

import com.github.kvnxiao.discord.client.botMention
import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("help")
@Alias(["help", "man"])
@Descriptor(
    description = "Displays the manual for a provided command alias.",
    usage = "%A <command alias>"
)
@Permissions(allowDirectMessaging = true)
class HelpCommand(
    private val prefixSettings: PrefixSettings,
    private val propertiesRegistry: PropertiesRegistry
) : Command {
    override fun execute(ctx: Context): Mono<Void> {
        val prefix = prefixSettings.getPrefixOrDefault(ctx.guild?.id)
        return Mono.justOrEmpty(propertiesRegistry.getPropertiesFromAlias(ctx.args.next()))
            .flatMap { (props, subAliases, pathList) ->
                ctx.channel.createMessage { spec ->
                    spec.setEmbed { embedSpec ->
                        val fullCommandPath = pathList.joinToString(separator = " ")
                        val replacement = if (props.permissions.requireBotMention) {
                            val mention = ctx.event.client.botMention()
                            "$mention `$fullCommandPath`"
                        } else {
                            "`$prefix$fullCommandPath`"
                        }

                        embedSpec.setTitle("Command Manual")
                            .addField("ID", props.id, true)
                            .addField("Aliases", props.aliases.joinToString(), true)
                            .addField("Permissions Required", props.formatPermissions(), false)
                            .addField("Description", props.descriptor.description, false)
                            .addField("Usage", formatUsage(props, replacement), false)
                            .addField("Sub-commands", formatSubCommands(subAliases, replacement), false)
                    }
                }
            }.then()
    }

    private fun formatUsage(props: CommandProperties, replacement: String): String =
        props.descriptor.usage.replace("%A", replacement)

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
