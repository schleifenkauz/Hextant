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
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.ImplicitReflectionSerializer
import java.io.File

class HttpPluginClient(private val url: String, private val downloadDirectory: File) : Marketplace {
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend inline fun <reified T, reified B : Any> get(url: String, body: B?): T? {
        val response = client.get<HttpResponse>(url) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                this.body = body
            }
        }
        return when (response.status) {
            HttpStatusCode.OK -> response.receive()
            HttpStatusCode.NotFound -> null
            else                    -> throw IllegalArgumentException(response.toString())
        }
    }

    private suspend inline fun <reified T> get(url: String): T? = get<T, Unit>(url, null)

    override suspend fun download(id: String): File? {
        val file = downloadDirectory.resolve("$id.jar")
        if (!file.exists()) {
            val response = client.get<HttpResponse>("$url/download/$id")
            if (response.status == HttpStatusCode.NotFound) return null
            check(response.status == HttpStatusCode.OK) { response }
            response.content.copyAndClose(file.writeChannel())
        }
        return file
    }

    override suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<Plugin> = get("$url/plugins", body = PluginSearch(searchText, limit, types, excluded))!!

    override suspend fun getPluginById(id: String): Plugin? = get("$url/plugins/$id")

    override suspend fun getImplementation(aspect: String, case: String): ImplementationBundle? =
        get("$url/implementations", body = ImplementationRequest(aspect, case))
}