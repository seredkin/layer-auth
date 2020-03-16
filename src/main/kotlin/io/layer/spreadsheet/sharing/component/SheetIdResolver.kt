package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.SheetReference
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Component
class SheetIdResolver: SheetIdService {
    private val id = { UUID.randomUUID().toString() }
    val cacheSheetName = ConcurrentHashMap<String, Pair<DataSheet, SheetReference>>()


    override fun getDataSheet(fileId: String, sheetName: String, authorId: UUID): DataSheet {
        if (!pattern.matcher(sheetName).matches())
            error("sheetName `$sheetName` doesn't match pattern `$validationRx`")
        val pageName = sheetName.substringBefore(rangeAfter)
        val key = fileId + fileIdBefore + pageName
        val dataSheet = DataSheet(id(), fileId, sheetName, authorId)
        val dataReference = SheetReference(fileId = fileId, sheetId = dataSheet.id)
        //Using Kotlin's CAS merge
        return cacheSheetName.getOrPut(key, { dataSheet to dataReference }).first
    }

    fun fetchByName(name: String): Pair<DataSheet, SheetReference> {
        return cacheSheetName.values.first { it.first.name == name }
    }

    override fun fetchByFileIdAndSheetName(fileId: String, sheetName: String): Optional<DataSheet> {
        return Optional.ofNullable(cacheSheetName["$fileId$fileIdBefore${sheetName.substringBefore(rangeAfter)}"]?.first)
    }

    companion object {
        const val fileIdBefore = "::"
        const val rangeAfter = "!"
        const val validationRx = "^('[\\w\\s]+'|[\\w\\s]+)(![A-Z]+[0-9]+)?(:[A-Z]+[0-9]+)?"
        val pattern = Pattern.compile(validationRx)!!
    }
}