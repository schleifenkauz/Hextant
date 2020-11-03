/**
 *@author Nikolaus Knop
 */

package hextant.main.plugins

import hextant.plugins.*
import hextant.plugins.PluginInfo.Type
import io.ktor.client.HttpClient
import io.ktor.client.call.TypeInfo
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.ConnectException
import kotlin.reflect.*

class HttpPluginClient(private val url: String, private val downloadDirectory: File) : Marketplace {
    private val client = HttpClient(Apache)

    private val local by lazy { LocalPluginRepository(downloadDirectory) }

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> get(url: String, type: KType, body: String? = null): T? = runBlocking {
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
    private inline fun <reified T, reified B : Any> get(url: String, body: B): T? =
        get<T>(url, typeOf<T>(), Json.encodeToString(body))

    @OptIn(ExperimentalStdlibApi::class)
    private inline fun <reified T> get(url: String): T? =
        get<T>(url, typeOf<T>())

    override suspend fun getJarFile(id: String): File? {
        val file = downloadDirectory.resolve("$id.jar")
        if (!file.exists()) {
            val response = try {
                client.get<HttpResponse>("$url/$id/download")
            } catch (e: ConnectException) {
                return null
            }
            if (response.status == HttpStatusCode.NotFound) return null
            check(response.status == HttpStatusCode.OK) { response }
            response.content.copyAndClose(file.writeChannel())
        }
        return file
    }

    override suspend fun <T : Any> get(property: PluginProperty<T>, pluginId: String): T? {
        val jar = downloadDirectory.resolve("$pluginId.jar")
        return if (jar.exists()) getInfo(property, pluginId)
        else get("$url/$pluginId/${property.name}", property.type)
    }

    override suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<String> = try {
        get("$url/plugins", body = PluginSearch(searchText, limit, types, excluded))!!
    } catch (e: ConnectException) {
        local.getPlugins(searchText, limit, types, excluded)
    }

    override suspend fun getImplementation(aspect: String, feature: String): ImplementationCoord? =
        try {
            get("$url/implementation", body = ImplementationRequest(aspect, feature))
        } catch (e: ConnectException) {
            local.getImplementation(aspect, feature)
        }

    override suspend fun availableProjectTypes(): List<LocatedProjectType> = try {
        get("$url/projectTypes")!!
    } catch (e: ConnectException) {
        local.availableProjectTypes()
    }

    override suspend fun getProjectType(name: String): LocatedProjectType? = try {
        get("$url/projectTypes/$name")
    } catch (e: ConnectException) {
        local.getProjectType(name)
    }

    @KtorExperimentalAPI
    override suspend fun upload(jar: File) {
        val parts = formData {
            append(jar.nameWithoutExtension, InputProvider(jar.length()) { jar.inputStream().asInput() })
        }
        client.submitFormWithBinaryData<Unit>("$url/upload", parts)
    }
}