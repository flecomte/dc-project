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

    /**
     * Convert vote to boolean
     */
    fun toBoolean(): Boolean = when (this) {
        GRANTED -> true
        DENIED -> false
    }
}

abstract class Voter {
    /**
     * A Shortcut for return a GrantedResponse
     */
    protected fun granted(message: String? = null, code: String? = null): GrantedResponse = GrantedResponse(this, message, code)
    /**
     * A Shortcut for return a DeniedResponse
     */
    protected fun denied(message: String, code: String): DeniedResponse = DeniedResponse(this, message, code)

    /**
     * Check all responses and return DENIED if one is DENIED
     *
     * If the list of responses is empty, return GRANTED
     */
    private fun VoterResponses.getOneResponse(): VoterResponse = this.firstOrNull { it.vote == Vote.DENIED } ?: granted()

    /**
     * An helper to convert a list of subject into one response
     */
    protected fun <S : List<T>, T> canAll(items: S, action: (T) -> VoterResponse): VoterResponse = items
        .map { action(it) }
        .getOneResponse()
}

/**
 * Throw an Exception if voter return a DENIED response
 */
fun <T : Voter> T.assert(action: T.() -> VoterResponse) {
    action().assert()
}

/**
 * Check all responses and return DENIED if one is DENIED
 *
 * If the list of responses is empty, return GRANTED
 */
fun VoterResponses.getOneResponse(): VoterResponse = this.firstOrNull { it.vote == Vote.DENIED } ?: GrantedResponse(first().voter)

/**
 * Throw an Exception if one response is DENIED
 */
fun VoterResponses.assert() = this.getOneResponse().assert()

class VoterDeniedException(private val voterResponses: VoterResponses) : Throwable(voterResponses.first().message) {
    constructor(voterResponse: VoterResponse) : this(listOf(voterResponse))

    /**
     * Get first response
     */
    fun first(): VoterResponse = voterResponses.first()

    /**
     * Check if the error code is present into the responses
     */
    fun hasErrorCode(code: String): Boolean = voterResponses
        .filter { it.vote == Vote.DENIED }
        .any { it.code == code }

    /**
     * Find and return the response than match with the error code
     */
    fun getErrorCode(code: String): VoterResponse? = voterResponses
        .firstOrNull { it.vote == Vote.DENIED && it.code == code }

    /**
     * Get a list of messages of all responses
     */
    fun getMessages(): List<String> = voterResponses
        .mapNotNull { it.message }

    /**
     * Get the first message
     */
    fun getFirstMessage(): String? = voterResponses
        .first()
        .message
}

/**
 * The response that all Voter method return
 * @see GrantedResponse
 * @see DeniedResponse
 */
sealed class VoterResponse(
    val vote: Vote,
    val voter: Voter,
    val message: String?,
    val code: String?
) {
    /**
     * Convert response as boolean
     */
    fun toBoolean(): Boolean = vote.toBoolean()

    /**
     * Throw Exception if response if DENIED
     */
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
