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
package com.github.kvnxiao.discord.command.prefix

import discord4j.core.`object`.entity.Guild

class PrefixSettings {
    companion object {
        const val DEFAULT_PREFIX: String = "!"
    }

    private val guildPrefix: MutableMap<Long, String> = mutableMapOf()

    fun loadGuildPrefixes() {
        // TODO
        guildPrefix[171867128314593280L] = "?"
    }

    fun getPrefixOrDefault(guildId: Long): String = guildPrefix[guildId] ?: DEFAULT_PREFIX

    fun getPrefixOrDefault(guild: Guild?): String = guildPrefix[guild?.id?.asLong()] ?: DEFAULT_PREFIX
}
