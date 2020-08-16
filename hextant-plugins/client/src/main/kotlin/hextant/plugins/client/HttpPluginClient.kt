/**
 *@author Nikolaus Knop
 */

package hextant.plugins.client

import hextant.plugins.*
import hextant.plugins.Plugin.Type
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ImplicitReflectionSerializer
import java.io.File

class HttpPluginClient(private val url: String, private val downloadDirectory: File) : Marketplace {
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private inline fun <reified T, reified B : Any> get(url: String, body: B?): T? = runBlocking {
        val response = client.get<HttpResponse>(url) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                this.body = body
            }
        }
        when (response.status) {
            HttpStatusCode.OK -> response.receive<T?>()
            HttpStatusCode.NotFound -> null
            else                    -> throw IllegalArgumentException(response.toString())
        }
    }

    override fun getJarFile(id: String): File? {
        val file = downloadDirectory.resolve("$id.jar")
        if (!file.exists()) {
            val response = runBlocking { client.get<HttpResponse>("$url/download/$id") }
            if (response.status == HttpStatusCode.NotFound) return null
            check(response.status == HttpStatusCode.OK) { response }
            runBlocking { response.content.copyAndClose(file.writeChannel()) }
        }
        return file
    }

    override fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<Plugin> = get("$url/plugins", body = PluginSearch(searchText, limit, types, excluded))!!

    override fun getImplementation(aspect: String, feature: String): ImplementationCoord? =
        get("$url/implementation", body = ImplementationRequest(aspect, feature))

    @KtorExperimentalAPI
    override fun upload(jar: File) = runBlocking {
        val parts = formData {
            append(jar.nameWithoutExtension, InputProvider(jar.length()) { jar.inputStream().asInput() })
        }
        client.submitFormWithBinaryData<Unit>("$url/upload", parts)
    }
}