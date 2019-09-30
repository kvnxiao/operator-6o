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
package com.github.kvnxiao.discord.koin

import mu.KLogger
import mu.KotlinLogging
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

private val logger: KLogger = KotlinLogging.logger { }

class ModulesLogger(level: Level = Level.INFO) : Logger(level) {

    override fun log(level: Level, msg: MESSAGE) {
        if (this.level <= level) {
            logOnLevel(msg)
        }
    }

    private fun logOnLevel(msg: MESSAGE) {
        when (this.level) {
            Level.DEBUG -> logger.debug(msg)
            Level.INFO -> logger.info(msg)
            Level.ERROR -> logger.error(msg)
        }
    }
}
