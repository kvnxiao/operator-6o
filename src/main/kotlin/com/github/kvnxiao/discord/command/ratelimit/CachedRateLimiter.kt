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
package com.github.kvnxiao.discord.command.ratelimit

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kvnxiao.discord.command.Id
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import mu.KLogger
import mu.KotlinLogging
import java.time.Duration

private val logger: KLogger = KotlinLogging.logger { }

data class CachedRateLimiter(
    private val commandId: Id,
    private val rateLimits: RateLimits
) : RateLimiter {

    private val userManager: Cache<Snowflake, Bucket> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(3))
        .expireAfterWrite(Duration.ofMinutes(3))
        .build()

    private val guildManager: Cache<Snowflake, Bucket> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .expireAfterWrite(Duration.ofMinutes(5))
        .build()

    override fun isNotRateLimited(guild: Guild?, user: User): Boolean =
        if (rateLimits.rateLimitOnGuild && guild != null) {
            guildManager.getOrCreateBucket(guild.id)?.tryConsume(1) ?: false
        } else {
            userManager.getOrCreateBucket(user.id)?.tryConsume(1) ?: false
        }

    private fun Cache<Snowflake, Bucket>.getOrCreateBucket(id: Snowflake): Bucket? =
        this.get(id) {
            logger.info {
                "Instantiating new rate-limit bucket for command [$commandId] (rateLimitOnGuild=${rateLimits.rateLimitOnGuild}, tokens=${rateLimits.tokensPerPeriod}, periodMs=${rateLimits.rateLimitPeriodMs}) [${if (rateLimits.rateLimitOnGuild) "guild" else "user"}:$id]"
            }
            Bucket.builder()
                .addLimit(
                    Bandwidth.simple(
                        rateLimits.tokensPerPeriod,
                        Duration.ofMillis(rateLimits.rateLimitPeriodMs)
                    )
                )
                .build()
        }
}
