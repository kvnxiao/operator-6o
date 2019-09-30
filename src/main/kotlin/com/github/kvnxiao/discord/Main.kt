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
package com.github.kvnxiao.discord

import com.github.kvnxiao.discord.client.Client
import com.github.kvnxiao.discord.koin.Modules
import com.github.kvnxiao.discord.koin.ModulesLogger
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        logger(ModulesLogger())

        environmentProperties()

        modules(
            listOf(
                Modules.validationModule,
                Modules.commandProcessingModule,
                Modules.commandsModule
            )
        )
    }

    Client().run()
}
