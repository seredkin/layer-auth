package io.layer.spreadsheet.sharing.api

interface SheetQueryResolver {
    fun byFileIdAndSheetNameAndAuthor(sheetQuery: SheetQuery): DataSheet
}
