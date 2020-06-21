package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import java.util.UUID

interface SheetIdService {
    fun fetchByFileIdAndSheetNameAndAuthor(fileId: String, sheetName: String, authorId: UUID): DataSheet
}
