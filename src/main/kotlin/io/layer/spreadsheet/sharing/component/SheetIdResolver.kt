package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.SheetReference
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Component
class SheetIdResolver {
    private val id = { UUID.randomUUID().toString() }
    val cacheSheetName = ConcurrentHashMap<String, Pair<DataSheet, SheetReference>>()


    fun getSheetId(sheetName: String, fileId: String): String {
        if (!pattern.matcher(sheetName).matches())
            error("sheetName `$sheetName` doesn't match pattern `$validationRx`")
        val pageName = sheetName.toLowerCase().substringBefore(rangeAfter)
        val key = fileId + fileIdBefore + normalize(pageName)
        val dataSheet = DataSheet(id(), fileId, sheetName)
        val dataReference = SheetReference(fileId, sheetName)
        return cacheSheetName.getOrPut(key, { dataSheet to dataReference }).first.id
    }

    private fun normalize(pageName: String) =
            pageName.toLowerCase()
                    .replace(" ", EMPTY)
                    .replace("'", EMPTY)
                    .trim()

    companion object {
        const val EMPTY = ""
        const val fileIdBefore = "::"
        const val rangeAfter = "!"
        const val validationRx = "^('[\\w\\s]+'|[\\w\\s]+)(![A-Z]+[0-9]+)?(:[A-Z]+[0-9]+)?"
        val pattern = Pattern.compile(validationRx)!!
    }
}