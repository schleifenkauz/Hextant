/**
 * @author Nikolaus Knop
 */

package hextant.plugins.server

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main(args: Array<String>) {
    check(args.size == 1) { "Invalid count of arguments" }
    val root = File(args[0])
    check(root.exists()) { "$root does not exist" }
    val repository = LocalPluginRepository(root)
    embeddedServer(Netty, 80, "localhost") {
        configure(repository)
    }.start()
}

private fun Application.configure(repository: PluginRepository) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/all") {
            call.respond(repository.getAllPlugins())
        }
        get("/{name}") {
            val pluginName = call.parameters["name"]!!
            val plugin = repository.getPluginById(pluginName)
            if (plugin == null) call.respond(NotFound, "No plugin with name '$pluginName'")
            else call.respond(plugin)
        }
        get("/download/{name}") {
            val pluginName = call.parameters["name"]!!
            val file = repository.getJarFile(pluginName)
            if (file == null) call.respond(NotFound, "No plugin with name '$pluginName'")
            else call.respondFile(file)
        }
    }
}