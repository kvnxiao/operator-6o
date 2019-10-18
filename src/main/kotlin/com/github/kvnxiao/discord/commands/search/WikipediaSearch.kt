package com.github.kvnxiao.discord.commands.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.http.HttpResponseHandler
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import discord4j.core.`object`.entity.Message
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.apache.http.client.utils.URIBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse

@JsonIgnoreProperties(ignoreUnknown = true)
private data class SearchResult(
    val query: Query
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Query(
    val search: List<SearchEntry>
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class SearchEntry(
    val ns: Int,
    val title: String,
    val pageid: Long,
    val size: Long,
    val wordcount: Long,
    val snippet: String
) {

    companion object {
        val SPAN_PATTERN = "(<span class=\"searchmatch\">|</span>)".toRegex()
    }

    val formattedSnippet: String
        get() = SPAN_PATTERN.replace(snippet, "`")

    val link: String
        get() = "https://en.wikipedia.org/wiki/${URLEncoder.encode(
            title.replace(" ", "_"),
            StandardCharsets.UTF_8.toString()
        )}"
}

@Component
@Id("wikipedia")
@Alias(["wiki"])
@Descriptor(
    description = "Searches for results on Wikipedia.",
    usage = "%A <query>"
)
class WikipediaSearch(
    private val objectMapper: ObjectMapper
) : Command, HttpResponseHandler {

    companion object {
        private val CUSTOM_SEARCH_URL_BUILDER: URIBuilder
            get() = URIBuilder()
                .setScheme("https")
                .setHost("en.wikipedia.org")
                .setPath("w/api.php")
                .addParameter("action", "query")
                .addParameter("list", "search")
                .addParameter("srlimit", "5")
                .addParameter("utf8", "")
                .addParameter("format", "json")
    }

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .flatMap { query: String ->
                HttpClient.create()
                    .get()
                    .uri(
                        CUSTOM_SEARCH_URL_BUILDER
                            .addParameter("srsearch", query)
                            .build()
                            .toASCIIString()
                    )
                    .responseSingle { response, body ->
                        handleResponse(ctx, response, body)
                    }
            }.then()

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        body.asInputStream()
            .map { objectMapper.readValue<SearchResult>(it) }
            .flatMap { search ->
                ctx.channel.createMessage { spec ->
                    spec.setEmbed { embedSpec ->
                        embedSpec.setTitle("${ReactionUnicode.MAG_RIGHT} Wikipedia Search")
                            .setDescription(formatMessage(ctx.args.arguments, search))
                    }
                }
            }

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        ctx.channel.createMessage("An error occurred while searching for ${ctx.args.arguments} on Wikipedia.\n${response.status().code()} - ${response.status().reasonPhrase()}")

    private fun formatMessage(query: String?, response: SearchResult): String {
        if (response.query.search.isEmpty()) return "**No results for `$query`**"
        val top = "**Results for: `$query`**\n"
        val body = response.query.search.joinToString(separator = "\n") { item ->
            "**[${item.title}](${item.link})**\n\u00A0\u00A0\u00A0\u00A0${item.formattedSnippet}"
        }
        return "$top\n$body"
    }
}
