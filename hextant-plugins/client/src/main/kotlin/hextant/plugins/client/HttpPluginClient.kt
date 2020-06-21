/**
 *@author Nikolaus Knop
 */

package hextant.plugins.client

import hextant.plugins.Plugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File

class HttpPluginClient(private val url: String, private val downloadDirectory: File) :
    PluginClient {
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    override suspend fun getAllPlugins(): List<String> = client.get("$url/all")

    override suspend fun getById(id: String): Plugin = client.get("$url/$id")

    override suspend fun getJarFile(pluginId: String): File {
        val file = downloadDirectory.resolve("$pluginId.jar")
        if (!file.exists()) {
            val response = client.request<HttpResponse>("$url/download/$pluginId")
            check(response.status.isSuccess()) { response }
            response.content.copyAndClose(file.writeChannel())
        }
        return file
    }
}