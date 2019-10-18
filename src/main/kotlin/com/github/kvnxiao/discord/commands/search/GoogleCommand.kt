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
package com.github.kvnxiao.discord.commands.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.env.Environment
import com.github.kvnxiao.discord.http.HttpResponseHandler
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import discord4j.core.`object`.entity.Message
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse

@JsonIgnoreProperties(ignoreUnknown = true)
private data class SearchResponse(
    val items: List<Item>,
    val kind: String,
    val searchInformation: SearchInformation
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Item(
    val link: String,
    val snippet: String,
    val title: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchInformation(
    val formattedSearchTime: String,
    val formattedTotalResults: String,
    val searchTime: Double,
    val totalResults: String
)

@Component
@Id("google")
@Alias(["g", "google"])
@Descriptor(
    description = "Searches for results on Google.",
    usage = "%A <query>"
)
class GoogleCommand(
    @Value(Environment.GOOGLE_SEARCH_ENGINE) private val googleSearchEngine: String,
    @Value(Environment.GOOGLE_API_KEY) private val googleApiKey: String,
    private val objectMapper: ObjectMapper
) : Command, HttpResponseHandler {

    companion object {
        private val CUSTOM_SEARCH_URL_BUILDER: URIBuilder
            get() = URIBuilder()
                .setScheme("https")
                .setHost("www.googleapis.com")
                .setPath("customsearch/v1")
    }

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .flatMap { query: String ->
                HttpClient.create()
                    .get()
                    .uri(
                        CUSTOM_SEARCH_URL_BUILDER
                            .addParameter("key", googleApiKey)
                            .addParameter("cx", googleSearchEngine)
                            .addParameter("q", query)
                            .build()
                            .toASCIIString()
                    )
                    .responseSingle { response, body ->
                        handleResponse(ctx, response, body)
                    }
            }.then()

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        body.asInputStream()
            .map { objectMapper.readValue<SearchResponse>(it) }
            .flatMap { search ->
                ctx.channel.createMessage { spec ->
                    spec.setEmbed { embedSpec ->
                        embedSpec.setTitle("${ReactionUnicode.MAG_RIGHT} Google Search")
                            .setDescription(formatMessage(ctx.args.arguments, search))
                    }
                }
            }

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        ctx.channel.createMessage("An error occurred while searching for ${ctx.args.arguments} on Google.\n${response.status().code()} - ${response.status().reasonPhrase()}")

    private fun formatMessage(query: String?, response: SearchResponse): String {
        if (response.items.isEmpty()) return "**No results for `$query`**"
        val top = "**Results for: `$query`**\n"
        val body = response.items.joinToString(separator = "\n") { item ->
            "**${item.title}**\n\u00A0\u00A0\u00A0\u00A0${item.link}"
        }
        return "$top\n$body"
    }
}
