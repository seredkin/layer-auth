package io.layer.spreadsheet.sharing.api

data class SheetQuery(
        val fileName: String,
        val sheetName: String,
        val authorId: String
)