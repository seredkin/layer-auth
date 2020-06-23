package io.layer.spreadsheet.sharing.api

data class SheetAddCommand(
        val id: String = id(),
        val fileName: String,
        val sheetName: String,
        val authorId: String
)