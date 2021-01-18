package fr.dcproject.dto

typealias Opinions = Map<String, Int>

interface Opinionable {
    val opinions: Opinions

    class Imp(parent: fr.dcproject.entity.Opinionable) : Opinionable {
        override val opinions: Opinions = parent.opinions
    }
}
