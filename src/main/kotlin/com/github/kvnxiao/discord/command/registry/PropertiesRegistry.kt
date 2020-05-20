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
package com.github.kvnxiao.discord.command.registry

import com.github.kvnxiao.discord.command.Alias
import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.DiscordCommand
import com.github.kvnxiao.discord.command.Id
import com.github.kvnxiao.discord.command.context.Arguments

data class PropertiesSearch(
    val properties: CommandProperties,
    val aliases: List<Alias>,
    val pathList: List<String>
)

class PropertiesRegistry(private val registryNode: RegistryNode) {
    val topLevelIds: List<Id>
        get() = registryNode.idEntries
    val topLevelProperties: List<CommandProperties>
        get() = topLevelIds.map { id -> registryNode.commandFromId(id)!!.properties }

    fun getPropertiesFromAlias(args: Arguments): PropertiesSearch? {
        val pathList: MutableList<String> = mutableListOf()
        var currArgs: Arguments = args
        var currNode: RegistryNode? = registryNode
        var currCommand: DiscordCommand? = null
        while (currNode != null && currArgs.alias.isNotEmpty() && currNode.subNodeFromAlias(currArgs.alias) != null) {
            currCommand = currNode.commandFromAlias(currArgs.alias)
            currNode = currNode.subNodeFromAlias(currArgs.alias)
            pathList.add(currArgs.alias)
            currArgs = currArgs.next()
        }
        return if (currCommand != null && currNode != null) {
            PropertiesSearch(currCommand.properties, currNode.aliasEntries.map { it.first }, pathList)
        } else {
            null
        }
    }

    fun getTopLevelPropertyById(id: Id): CommandProperties? =
        registryNode.commandFromId(id)?.properties
}
