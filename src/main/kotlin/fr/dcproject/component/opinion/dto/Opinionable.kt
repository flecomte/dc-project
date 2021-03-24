package fr.dcproject.component.opinion.dto

typealias Opinions = Map<String, Int>

interface Opinionable {
    val opinions: Opinions

    class Imp(parent: fr.dcproject.component.opinion.entity.Opinionable) : Opinionable {
        override val opinions: Opinions = parent.opinions
    }
}
