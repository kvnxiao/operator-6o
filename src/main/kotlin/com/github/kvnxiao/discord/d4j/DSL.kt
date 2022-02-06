/**
 *   Copyright 2020 Ze Hao (Kevin) Xiao
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
package com.github.kvnxiao.discord.d4j

import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec

@DslMarker
annotation class CreateMessageDSL

@CreateMessageDSL
fun embed(block: EmbedCreateSpec.Builder.() -> Unit): EmbedCreateSpec =
    EmbedCreateSpec.builder().apply(block).build()

@CreateMessageDSL
fun message(block: MessageCreateSpec.Builder.() -> Unit): MessageCreateSpec =
    MessageCreateSpec.builder().apply(block).build()
