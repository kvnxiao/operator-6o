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
import discord4j.core.`object`.util.Snowflake

class PrefixSettings {
    companion object {
        const val DEFAULT_PREFIX: String = "!"
    }

    private val guildPrefix: MutableMap<Snowflake, String> = mutableMapOf()

    fun loadGuildPrefixes() {
        // TODO
        guildPrefix[Snowflake.of(171867128314593280L)] = "?"
    }

    /**
     * Gets the command alias prefix for a specified guild based on a guild's ID snowflake value.
     * Returns [DEFAULT_PREFIX] if the guild ID does not match with the custom internal mapping.
     */
    fun getPrefixOrDefault(guildId: Snowflake): String = guildPrefix[guildId] ?: DEFAULT_PREFIX

    /**
     * Gets the command alias prefix for a specified guild based on a nullable Guild parameter.
     * Returns [DEFAULT_PREFIX] if the guild provided is null.
     */
    fun getPrefixOrDefault(guild: Guild?): String = guildPrefix[guild?.id] ?: DEFAULT_PREFIX
}
