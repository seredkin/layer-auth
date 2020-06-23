package io.layer.spreadsheet.sharing.api

interface SheetCommandService {

    fun addDataSheet(sheetAddCommand: SheetAddCommand):DataSheet
}
