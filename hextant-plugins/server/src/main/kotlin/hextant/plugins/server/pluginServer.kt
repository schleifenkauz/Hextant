/**
 * @author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.*
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.request.receive
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

private fun Application.configure(marketplace: Marketplace) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/plugins/") {
            val (searchText, limit, types, excluded) = call.receive<PluginSearch>()
            call.respond(marketplace.getPlugins(searchText, limit, types, excluded))
        }
        get("/{id}") {
            val id = call.parameters["id"]!!
            val plugin = marketplace.getPluginById(id)
            if (plugin == null) call.respond(NotFound, "No plugin with name '$id'")
            else call.respond(plugin)
        }
        get("/download/{id}") {
            val id = call.parameters["id"]!!
            val file = marketplace.getJarFile(id)
            if (file == null) call.respond(NotFound, "No plugin with name '$id'")
            else call.respondFile(file)
        }
        get("/implementation") {
            val (aspect, case) = call.receive<ImplementationRequest>()
            val bundle = marketplace.getImplementation(aspect, case)
            if (bundle == null) call.respond(NotFound, "No implementation for $aspect:$case")
            else call.respond(bundle)
        }
    }
}