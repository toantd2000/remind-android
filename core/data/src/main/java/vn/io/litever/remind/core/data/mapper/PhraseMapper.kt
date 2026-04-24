package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.model.PhraseEntity
import vn.io.litever.remind.core.model.Phrase

fun PhraseEntity.toModel(): Phrase {
    return Phrase(
        id = id,
        content = content,
        categoryId = categoryId,
        isCustom = isCustom,
        isShared = isShared,
        alarmId = alarmId
    )
}

fun Phrase.toEntity(): PhraseEntity {
    return PhraseEntity(
        id = id,
        content = content,
        categoryId = categoryId,
        isCustom = isCustom,
        isShared = isShared,
        alarmId = alarmId
    )
}










