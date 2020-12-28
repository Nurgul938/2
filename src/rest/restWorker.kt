package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import repo.*

fun Application.restWorker(
    workerRepo: Repo<Worker>,
    workerSerializer: KSerializer<Worker>,
    drugRepo: Repo<Drug>,
    drugSerializer: KSerializer<Drug>,
    cartRepo: Repo<Cart>,
    cartSerializer: KSerializer<Cart>
) {
    routing {
        route("/worker") {
            post {
                parseBody(workerSerializer)?.let { elem ->
                    if (workerRepo.read().filter { it.workername == elem.workername }.isEmpty()) {
                        if (workerRepo.create(elem)) {
                            val user = workerRepo.read().find { it.workername == elem.workername }!!
                            call.respond(user)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } else {
                        call.respond(HttpStatusCode.Conflict, "Пользователь с таким именем уже существует")
                    }
                } ?: call.respond(HttpStatusCode.BadRequest, "Передан неправильный объект User")
            }
        }
        route("/worker/{id}") {
            get {
                call.respond(
                    parseId()?.let { id ->
                        workerRepo.read(id) ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            put {
                parseBody(workerSerializer)?.let { elem ->
                    parseId()?.let { id ->
                        if (workerRepo.update(id, elem))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    }
                } ?: HttpStatusCode.BadRequest
            }
            delete {
                parseId()?.let { id ->
                    if (workerRepo.delete(id))
                        HttpStatusCode.OK
                    else
                        HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }

        route("/worker/drugs") {
            post {
                parseBody(drugSerializer)?.let { elem ->
                    if (drugRepo.create(elem))
                        HttpStatusCode.OK
                    else
                        HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
            get {
                val drugs = drugRepo.read()
                if (drugs.isNotEmpty()) {
                    call.respond(drugs)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        route("/worker/{id}/drugs") {
            get {
                val books = drugRepo.read().filter { it.workerId == parseId() }
                if (books.isNotEmpty()) {
                    call.respond(books)
                } else {
                    call.respond(listOf<Drug>())
                }
            }
        }

        route("/worker/drugs/{id}") {
            get {
                parseId()?.let { id ->
                    drugRepo.read(id)?.let { elem ->
                        call.respond(elem)
                    } ?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
            put {
                parseBody(drugSerializer)?.let { elem ->
                    parseId()?.let { id ->
                        if (drugRepo.update(id, elem))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    }
                } ?: HttpStatusCode.BadRequest
            }
            delete {
                parseId()?.let { id ->
                    if (drugRepo.delete(id))
                        HttpStatusCode.OK
                    else
                        HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }

        route("/worker/{id}/drugs/cart") {
            get {
                val cartsArray = cartRepo.read()
                val carts = drugRepo.read().map { drug ->
                    cartsArray.any {
                        it.drugId == drug.id && it.workerId == parseId()
                    }
                }
                call.respond(carts)
            }
        }



    }
}


fun PipelineContext<Unit, ApplicationCall>.parseId(id: String = "id") =
    call.parameters[id]?.toIntOrNull()

fun PipelineContext<Unit, ApplicationCall>.drugId(id: String = "drugId") =
    call.parameters[id]?.toIntOrNull()

suspend fun <T> PipelineContext<Unit, ApplicationCall>.parseBody(
    serializer: KSerializer<T>
) =
    try {
        Json.decodeFromString(
            serializer,
            call.receive()
        )
    } catch (e: Throwable) {
        null
    }
