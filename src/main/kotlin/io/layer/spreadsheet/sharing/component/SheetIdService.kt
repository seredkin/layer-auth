package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import java.util.Optional
import java.util.UUID

interface SheetIdService {

    fun getDataSheet(fileId: String, sheetName: String, authorId: UUID): DataSheet
    fun fetchByFileIdAndSheetName(fileId: String, sheetName: String): Optional<DataSheet>
}
