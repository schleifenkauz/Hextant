/**
 *@author Nikolaus Knop
 */

package hextant.plugins.client

import hextant.plugins.*
import hextant.plugins.PluginInfo.Type
import io.ktor.client.HttpClient
import io.ktor.client.call.TypeInfo
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
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.*

class HttpPluginClient(private val url: String, private val downloadDirectory: File) : Marketplace {
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {})
        }
    }

    private var counter = 0

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> get(url: String, type: KType, body: Any? = null): T? = runBlocking {
        val response = client.get<HttpResponse>(url) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                this.body = body
            }
        }

        when (response.status) {
            HttpStatusCode.OK -> {
                val info = TypeInfo(type.classifier as KClass<*>, type.javaType, type)
                response.call.receive(info) as T
            }
            HttpStatusCode.NotFound -> null
            else                    -> throw IllegalArgumentException(response.toString())
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private inline fun <reified T> get(url: String, body: Any? = null): T? = get<T>(url, typeOf<T>(), body)

    override suspend fun getJarFile(id: String): File? {
        val file = downloadDirectory.resolve("${counter++}.jar")
        if (!file.exists()) {
            val response = client.get<HttpResponse>("$url/$id/download")
            if (response.status == HttpStatusCode.NotFound) return null
            check(response.status == HttpStatusCode.OK) { response }
            response.content.copyAndClose(file.writeChannel())
        }
        return file
    }

    override suspend fun <T : Any> get(property: PluginProperty<T>, pluginId: String): T? =
        get("$url/$pluginId/${property.name}", property.type)

    override suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<String> = get("$url/plugins", body = PluginSearch(searchText, limit, types, excluded))!!

    override suspend fun getImplementation(aspect: String, feature: String): ImplementationCoord? =
        get("$url/implementation", body = ImplementationRequest(aspect, feature))

    override suspend fun availableProjectTypes(): List<LocatedProjectType> = get("$url/projectTypes")!!

    override suspend fun getProjectType(name: String): LocatedProjectType? = get("$url/projectTypes/$name")

    @KtorExperimentalAPI
    override suspend fun upload(jar: File) {
        val parts = formData {
            append(jar.nameWithoutExtension, InputProvider(jar.length()) { jar.inputStream().asInput() })
        }
        client.submitFormWithBinaryData<Unit>("$url/upload", parts)
    }
}