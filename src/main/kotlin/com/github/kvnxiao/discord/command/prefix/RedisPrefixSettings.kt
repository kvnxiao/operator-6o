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

import discord4j.rest.util.Snowflake
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisPrefixSettings(
    private val redis: ReactiveRedisTemplate<String, String>
) : PrefixSettings {
    private val guildPrefix: MutableMap<Snowflake, String> = mutableMapOf()

    override fun loadPrefixForGuild(guildId: Snowflake): Mono<String> =
        redis.opsForHash<String, String>().get("guild:${guildId.asString()}", "prefix")
            .doOnNext { result -> guildPrefix[guildId] = result }

    override fun setPrefixForGuild(guildId: Snowflake, value: String): Mono<Boolean> =
        redis.opsForHash<String, String>().put("guild:${guildId.asString()}", "prefix", value)
            .doOnNext { success ->
                if (success) {
                    guildPrefix[guildId] = value
                }
            }

    /**
     * Gets the command alias prefix for a specified guild based on a guild's ID snowflake value.
     * Returns [PrefixSettings.DEFAULT_PREFIX] if the guild ID does not match with the custom internal mapping.
     */
    override fun getPrefixOrDefault(guildId: Snowflake?): String =
        guildId?.let { guildPrefix[guildId] } ?: PrefixSettings.DEFAULT_PREFIX
}
