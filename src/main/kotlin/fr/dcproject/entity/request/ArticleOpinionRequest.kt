package fr.dcproject.entity.request

import fr.dcproject.entity.Citizen
import fr.dcproject.entity.OpinionArticle
import fr.dcproject.entity.OpinionChoiceRef
import fr.dcproject.entity.TargetRef
import fr.dcproject.repository.Article
import fr.dcproject.repository.OpinionChoice
import fr.dcproject.utils.toUUID
import org.koin.core.KoinComponent
import org.koin.core.get

class ArticleOpinionRequest(
    target: String,
    opinionChoice: String
) : RequestBuilderWithCreator<Citizen, OpinionArticle>, KoinComponent {
    val target = TargetRef(target.toUUID())
    val opinionChoice = OpinionChoiceRef(opinionChoice.toUUID())

    override fun create(citizen: Citizen): OpinionArticle {
        return OpinionArticle(
            choice = get<OpinionChoice>().findOpinionChoiceById(opinionChoice.id)!!,
            target = get<Article>().findById(target.id)!!,
            createdBy = citizen
        )
    }
}