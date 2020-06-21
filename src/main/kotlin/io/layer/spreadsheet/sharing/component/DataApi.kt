package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.FileReference
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SheetReference
import org.jetbrains.exposed.sql.ResultRow

internal fun rowToDataSheet(it: ResultRow): DataSheet {
    return DataSheet(
            id = it[TSheetName.id],
            fileId = it[TSheetName.fileId].toString(),
            name = it[TSheetName.name],
            authorId = it[TSheetName.authorId]
    )
}

internal fun rowToSharingGroup(it: ResultRow, dataReference: DataReference<String, DataRange?>): SharingGroup {
    return with(TSharingGroup) {
        SharingGroup(
                id = it[id].toString(),
                authorId = it[authorId].toString(),
                permission = Permission.of(read = it[permissionRead], write = it[permissionWrite], share = it[permissionShare]),
                users = setOf(),
                data = dataReference
        )
    }
}


internal fun rowToDataReference(row: ResultRow): DataReference<String, DataRange?> {
    with(TDataReference) {
        return when {
            row[sheetId] == null -> FileReference(id = row[id].toString(), fileId = row[fileId])
            row[rangeCellsSet] == null && row[rangeCellsBetween] == null -> SheetReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString()
            )
            row[rangeCellsSet] != null -> RangeReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString(),
                    range = DataRange.fromStringSet(row[rangeCellsSet]!!)
            )
            else -> RangeReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString(),
                    range = DataRange.fromStringSet(row[rangeCellsBetween]!!)
            )
        }

    }
}


