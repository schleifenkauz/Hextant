/**
 * @author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.*
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.content.*
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.*
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

private fun Application.configure(repo: LocalPluginRepository) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/plugins/") {
            val (searchText, limit, types, excluded) = call.receive<PluginSearch>()
            call.respond(repo.getPlugins(searchText, limit, types, excluded))
        }
        get("/{id}") {
            val id = call.parameters["id"]!!
            val plugin = repo.getPluginById(id)
            if (plugin == null) call.respond(NotFound, "No plugin with name '$id'")
            else call.respond(plugin)
        }
        get("/download/{id}") {
            val id = call.parameters["id"]!!
            val file = repo.getJarFile(id)
            if (file == null) call.respond(NotFound, "No plugin with name '$id'")
            else call.respondFile(file)
        }
        get("/implementation") {
            val (aspect, case) = call.receive<ImplementationRequest>()
            val bundle = repo.getImplementation(aspect, case)
            if (bundle == null) call.respond(NotFound, "No implementation for $aspect:$case")
            else call.respond(bundle)
        }
        post("/upload") {
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem) {
                    repo.upload(part.streamProvider(), part.originalFileName!!)
                }
            }
        }
        get("/projectTypes") {
            call.respond(repo.availableProjectTypes())
        }
    }
}