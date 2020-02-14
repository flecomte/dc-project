package fr.dcproject.entity.request

import io.ktor.application.ApplicationCall

interface Request

interface RequestBuilder<E> {
    suspend fun getContent(call: ApplicationCall): E
}

suspend fun <E> ApplicationCall.getContent(builder: RequestBuilder<E>) = builder.getContent(this)
