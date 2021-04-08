package fr.dcproject.common.validation

import io.konform.validation.ValidationBuilder

fun ValidationBuilder<String>.passwordScore(minScore: Int) =
    addConstraint("is not enough strong. Use Upper case, Lower case and special characters or juste use more characters.") { value ->
        value.passwordScore() >= minScore
    }

fun String.passwordScore(): Int {
    var score: Int = length
    val alphaNum = ('a'..'z').toList() + ('A'..'Z').toList() + ('0'..'9').toList()
    val specialCount = length - toList().intersect(alphaNum).size
    score += specialCount.let { if (it > 3) 3 else it }

    val hasAlphaLower = toList().intersect(('a'..'z').toList()).size.let { if (it > 2) 2 else it }
    val hasAlphaUpper = toList().intersect(('A'..'Z').toList()).size.let { if (it > 2) 2 else it }
    val hasNum = toList().intersect(('0'..'9').toList()).size.let { if (it > 2) 2 else it }
    score += (hasAlphaLower + hasAlphaUpper + hasNum - 2) * 2

    return score
}
