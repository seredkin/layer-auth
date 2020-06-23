package io.layer.spreadsheet.sharing.api

data class SheetAddCommand(
        val fileName: String,
        val sheetName: String,
        val authorId: String
)