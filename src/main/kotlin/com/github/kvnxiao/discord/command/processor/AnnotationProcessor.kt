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

import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.DiscordCommand
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.annotation.RateLimits
import com.github.kvnxiao.discord.command.annotation.SubCommand
import com.github.kvnxiao.discord.command.descriptor.Descriptor as CommandDescriptor
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import com.github.kvnxiao.discord.command.permission.Permissions as CommandPermissions
import com.github.kvnxiao.discord.command.ratelimit.RateLimits as CommandRateLimits
import com.github.kvnxiao.discord.command.registry.RegistryNode
import discord4j.core.`object`.util.PermissionSet
import kotlin.reflect.KClass
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger { }

typealias CommandKClass = KClass<out CommandExecutable>

class AnnotationProcessor {

    companion object {
        private val KCLASS_COMPARATOR: Comparator<KClass<*>> = Comparator { o1, o2 ->
            o1.toString().compareTo(o2.toString())
        }
    }

    fun process(commands: List<CommandExecutable>, rootRegistry: RegistryNode) {
        val subCommandEdgeMap = mutableMapOf<DiscordCommand, List<CommandKClass>>()
        val subCommandClassSet = mutableSetOf<CommandKClass>()
        val classToCommandMap = mutableMapOf<CommandKClass, DiscordCommand>()

        commands.forEach { executable ->
            val commandKClass = executable::class
            val annotations = commandKClass.annotations
            // Require non-null command id
            val id = annotations.find<Id>()?.id
            require(id != null) { "@Id annotation must exist for command $commandKClass" }
            // Parse for aliases, descriptor, rate-limits, and permissions (using defaults if non-existent)
            val aliases = annotations.find<Alias>()?.aliases?.toSet() ?: setOf(id)
            val descriptor = annotations.getCommandDescriptor()
            val rateLimits = annotations.getCommandRateLimits()
            val permissions = annotations.getCommandPermissions()
            // Parse for sub-commands
            val subCommandKClasses = annotations.getSubCommandClasses(commandKClass)
            // Create command package from parsed information
            val command = DiscordCommand(
                CommandProperties(id, aliases, descriptor, rateLimits, permissions),
                executable
            )
            // Add list of sub-command classes if sub-commands do exist
            subCommandKClasses?.let {
                subCommandClassSet.addAll(it)
                subCommandEdgeMap[command] = it // create directed edge of parent command to sub-commands children list
            }

            classToCommandMap[commandKClass] = command
        }

        val rootCommandClassSet: MutableSet<CommandKClass> = classToCommandMap.keys
            .toMutableSet()
            .apply { removeAll(subCommandClassSet) }

        // Debug logs for annotation processing status
        logger.debug { "Processed ${commands.size} command executable annotations." }
        logger.debug {
            "Constructed ${rootCommandClassSet.size} root-level commands:\n${rootCommandClassSet.prettyString(
                classToCommandMap)}"
        }
        logger.debug {
            "Constructed ${subCommandClassSet.size} sub-commands:\n${subCommandClassSet.prettyString(
                classToCommandMap)}"
        }

        // Get map<KClass, DiscordCommand> of rootCommands
        val rootCommands = classToCommandMap.filterKeys { it in rootCommandClassSet }
        // Remove root-commands from main class:command map (so that the leftovers are for sub-commands only)
        rootCommands.keys.forEach { classToCommandMap.remove(it) }
        // Register root-commands individually, with their corresponding sub-commands
        rootCommands.values.forEach { registerCommand(it, rootRegistry, classToCommandMap, subCommandEdgeMap) }

        require(
            classToCommandMap.isEmpty()) { "Expected the class:command mapping to be empty but got leftovers (a cycle could exist, causing no root-level command to be present): $classToCommandMap" }
        require(
            subCommandEdgeMap.isEmpty()) { "Expected the sub-command edge map to be empty but got leftovers: $subCommandEdgeMap" }
    }

    private fun registerCommand(
        currentCommand: DiscordCommand,
        currentRegistry: RegistryNode,
        classToCommandMap: MutableMap<CommandKClass, DiscordCommand>,
        subCommandEdgeMap: MutableMap<DiscordCommand, List<CommandKClass>>
    ) {
        val subRegistry: RegistryNode = currentRegistry.register(currentCommand)
        val subCommandsClassList = subCommandEdgeMap.remove(currentCommand)
        if (subCommandsClassList != null) {
            // Get sub-commands and remove from the class:command map (to avoid cycles)
            val subCommands = subCommandsClassList.map { subKClass ->
                classToCommandMap.remove(subKClass) ?: throw IllegalArgumentException(
                    """The sub-command class $subKClass does not exist in the class:command mapping.
                      |Either the command was not added as a dependency within a Koin module,
                      |or it has already been processed and thus a cycle was detected in the command graph.""".trimMargin()
                )
            }
            // Register sub-commands for current command (including their own sub-commands, if exists)
            subCommands.forEach { subCommand ->
                registerCommand(subCommand, subRegistry, classToCommandMap, subCommandEdgeMap)
            }
        }
    }

    private inline fun <reified T : Annotation> List<Annotation>.find(): T? = this.find { it is T } as? T

    private fun List<Annotation>.getCommandDescriptor(): CommandDescriptor =
        this.find<Descriptor>()?.run { CommandDescriptor(description, usage) } ?: CommandDescriptor()

    private fun List<Annotation>.getCommandRateLimits(): CommandRateLimits =
        this.find<RateLimits>()?.run {
            CommandRateLimits(rateLimitOnGuild, tokensPerPeriod, rateLimitPeriodMs)
        } ?: CommandRateLimits()

    private fun List<Annotation>.getCommandPermissions(): CommandPermissions =
        this.find<Permissions>()?.run {
            CommandPermissions(
                requireBotOwner,
                requireGuildOwner,
                requireBotMention,
                allowDirectMessaging,
                requireDirectMessaging,
                removeInvocationMessage,
                PermissionSet.of(*permSet)
            )
        } ?: CommandPermissions()

    private fun List<Annotation>.getSubCommandClasses(commandKClass: CommandKClass): List<CommandKClass>? =
        this.find<SubCommand>()?.subCommands?.let { subCommandKClasses ->
            // Check does not contain itself
            require(!subCommandKClasses.toSet().contains(commandKClass)) {
                "Command $commandKClass cannot contain a sub-command referencing itself."
            }
            // Check does not contain duplicate sub-command references
            val duplicates = subCommandKClasses.groupingBy(CommandKClass::qualifiedName)
                .eachCount()
                .filter { count -> count.value > 1 }
            require(duplicates.isEmpty()) {
                "Command $commandKClass cannot contain duplicate sub-command classes. Please fix the following: ${duplicates.keys}"
            }
            subCommandKClasses.toList()
        }

    private fun Collection<CommandKClass>.prettyString(
        classToCommandMap: MutableMap<CommandKClass, DiscordCommand>
    ): String =
        this.toSortedSet(KCLASS_COMPARATOR)
            .joinToString(
                separator = "\n  ",
                prefix = "[\n  ",
                postfix = "\n]"
            ) { "${classToCommandMap[it]?.properties?.id}: $it" }
}
