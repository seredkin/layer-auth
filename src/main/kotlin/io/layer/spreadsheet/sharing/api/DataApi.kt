package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_CELL_RANGE
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_CELL_SET
import java.util.*

val id = { UUID.randomUUID().toString() }
val uuid = { UUID.randomUUID() }

data class DataFile(val id: String, val name: String, val authorId: String)
data class DataSheet(val id: UUID = uuid(), val fileId: String, val name: String, val authorId: UUID)
data class DataCell(val x: Int, val y: Int, val sheetId: String)
data class DataRange(
        val cellSet: Set<DataCell> = setOf(),
        val cellRange: Set<DataCell> = setOf()
) {
    fun type(): String = when {
        cellSet.isNotEmpty() -> TYPE_CELL_SET
        else -> TYPE_CELL_RANGE
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun creator(cellSet: Set<DataCell>?, cellRange: Set<DataCell>?): DataRange = when {
            cellSet != null && cellSet.isNotEmpty() -> DataRange(cellSet = cellSet)
            else -> DataRange(cellRange = cellRange!!)
        }

        fun fromStringSet(daString: String) = DataRange(setOf(), setOf())
        fun fromStringBetween(daString: String) = DataRange(setOf(), setOf())
        fun asStringSet(range: DataRange) = range.toString()
        fun asStringBetween(range: DataRange) = range.toString()
    }
}





