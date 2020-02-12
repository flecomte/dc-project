package fr.dcproject.entity.request

import fr.dcproject.entity.CitizenRef
import fr.postgresjson.entity.EntityI

interface Request

interface RequestBuilder<E: EntityI> : Request {
    fun create(): E
}

interface RequestBuilderWithCreator<C: CitizenRef, E: EntityI> : Request {
    fun create(citizen: C): E
}
