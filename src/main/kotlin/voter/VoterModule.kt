package fr.dcproject.voter

/** Responses of voters */
enum class Vote {
    GRANTED,
    DENIED;

    /** Helper to convert true/false to GRANTED/DENIED */
    companion object {
        fun toVote(lambda: () -> Boolean): Vote = when (lambda()) {
            true -> GRANTED
            false -> DENIED
        }
    }

    fun toBoolean(): Boolean = when (this) {
        GRANTED -> true
        DENIED -> false
    }
}

abstract class Voter {
    protected fun granted(message: String? = null, code: String? = null): GrantedResponse = GrantedResponse(this, message, code)
    protected fun denied(message: String, code: String): DeniedResponse = DeniedResponse(this, message, code)

    private fun VoterResponses.getOneResponse(): VoterResponse = this.firstOrNull { it.vote == Vote.DENIED } ?: granted()

    protected fun <S : List<T>, T> canAll(items: S, action: (T) -> VoterResponse): VoterResponse = items
        .map { action(it) }
        .getOneResponse()
}

fun <T : Voter> T.assert(action: T.() -> VoterResponse) {
    action().assert()
}

fun VoterResponses.getOneResponse(): VoterResponse = this.firstOrNull { it.vote == Vote.DENIED } ?: GrantedResponse(first().voter)

fun VoterResponses.assert() = this.getOneResponse().assert()

class VoterDeniedException(private val voterResponses: VoterResponses) : Throwable(voterResponses.first().message) {
    constructor(voterResponse: VoterResponse) : this(listOf(voterResponse))

    fun first(): VoterResponse = voterResponses.first()

    fun hasErrorCode(code: String): Boolean = voterResponses
        .filter { it.vote == Vote.DENIED }
        .any { it.code == code }

    fun getErrorCode(code: String): VoterResponse? = voterResponses
        .firstOrNull { it.vote == Vote.DENIED && it.code == code }

    fun getMessages(): List<String> = voterResponses
        .mapNotNull { it.message }

    fun getFirstMessage(): String? = voterResponses
        .first()
        .message
}

sealed class VoterResponse(
    val vote: Vote,
    val voter: Voter,
    val message: String?,
    val code: String?
) {
    fun toBoolean(): Boolean = vote.toBoolean()
    fun assert() {
        if (this.vote == Vote.DENIED) {
            throw VoterDeniedException(this)
        }
    }
}

class GrantedResponse(
    voter: Voter,
    message: String? = null,
    code: String? = null
) : VoterResponse(Vote.GRANTED, voter, message, code)

class DeniedResponse(
    voter: Voter,
    message: String,
    code: String
) : VoterResponse(Vote.DENIED, voter, message, code)

typealias VoterResponses = List<VoterResponse>
