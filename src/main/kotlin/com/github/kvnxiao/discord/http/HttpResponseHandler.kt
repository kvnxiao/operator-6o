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
package com.github.kvnxiao.discord.http

import com.github.kvnxiao.discord.command.context.Context
import discord4j.core.`object`.entity.Message
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClientResponse

interface HttpResponseHandler {
    fun handleResponse(
        ctx: Context,
        query: String,
        response: HttpClientResponse,
        body: ByteBufMono
    ): Mono<Message> =
        if (response.status().code() in 200..299) {
            handleInputStream(ctx, query, body)
        } else {
            handleError(ctx, query, response)
        }

    fun handleInputStream(ctx: Context, query: String, body: ByteBufMono): Mono<Message>

    fun handleError(ctx: Context, query: String, response: HttpClientResponse): Mono<Message>
}
