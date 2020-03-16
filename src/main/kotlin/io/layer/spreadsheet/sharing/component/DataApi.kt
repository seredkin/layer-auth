package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import org.jetbrains.exposed.sql.ResultRow

data class UserName(
        val id: String,
        val email: String
)

internal fun rowToUserName(it: ResultRow): UserName {
    return UserName(
            id = it[TUserEmail.id].toString(),
            email = it[TUserEmail.email]
    )
}

data class SheetName(
        val id: String,
        val fileId: String,
        val name: String
)

internal fun rowToDataSheet(it: ResultRow): DataSheet {
    return DataSheet(
            id = it[TSheetName.id].toString(),
            fileId = it[TSheetName.fileId].toString(),
            name = it[TSheetName.name],
            authorId = it[TSheetName.authorId]
    )
}


