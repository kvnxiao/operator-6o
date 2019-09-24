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

/**
 * A data class containing rate-limit settings for a command.
 */
data class RateLimits(
    val rateLimitOnGuild: Boolean = RATELIMIT_ON_GUILD,
    val tokensPerPeriod: Long = TOKENS_PER_PERIOD,
    val rateLimitPeriodMs: Long = RATELIMIT_PERIOD_MS
) {

    companion object {
        const val RATELIMIT_ON_GUILD = false
        const val TOKENS_PER_PERIOD: Long = 3
        const val RATELIMIT_PERIOD_MS: Long = 1000
    }
}
