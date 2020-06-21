package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_CELL_RANGE
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_CELL_SET
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_FILE
import io.layer.spreadsheet.sharing.api.DataReference.Companion.TYPE_SHEET
import java.util.UUID

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

interface Permission {
    val read: Boolean
    val write: Boolean
    val share: Boolean

    companion object {
        val READ: Permission = ReadPermission(read = true, write = false, share = false)
        val WRITE: Permission = WritePermission(read = true, write = true, share = false)
        val SHARE: Permission = SharePermission(read = true, write = true, share = true)
        @JsonCreator
        @JvmStatic
        fun of(read: Boolean, write: Boolean, share: Boolean): Permission = when {
            share && write && read -> SHARE
            write && read -> WRITE
            read -> READ
            else -> error("Wrong permission values:$read, write:$write, share:$share")
        }
    }
}

private class ReadPermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission

private class WritePermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission

private class SharePermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission

interface DataReference<S, out D> {
    val id: S?
    val fileId: S
    val sheetId: S?
    val range: D?

    fun type(): String
    companion object {
        const val TYPE_FILE = "FILE"
        const val TYPE_SHEET = "SHEET"
        const val TYPE_CELL_SET = "CELL_SET"
        const val TYPE_CELL_RANGE = "CELL_RANGE"
        @JsonCreator
        @JvmStatic
        fun creator(fileId: String, sheetId: String?, range: DataRange?): DataReference<String, DataRange?> = when {
            range == null && sheetId == null -> FileReference(fileId = fileId)
            range != null && sheetId != null -> RangeReference(fileId = fileId, sheetId = sheetId, range = range)
            sheetId != null -> SheetReference(fileId = fileId, sheetId = sheetId)
            else -> error("Range cannot be referenced without a sheetId: $fileId, $sheetId, $range")
        }
    }

}

data class FileReference(
        override val id: String?=null,
        override val fileId: String,
        override val sheetId: String? = null,
        override val range: DataRange? = null)
    : DataReference<String, DataRange> {
    override fun type() = TYPE_FILE
}

data class SheetReference(
        override val id: String? = null,
        override val fileId: String,
        override val sheetId: String,
        override val range: DataRange? = null)
    : DataReference<String, DataRange> {
    override fun type() = TYPE_SHEET
}

data class RangeReference(
        override val id: String? = null,
        override val fileId: String,
        override val sheetId: String,
        override val range: DataRange)
    : DataReference<String, DataRange> {
    override fun type() = range.type()

}


data class AddPermissionCommand(
        val sharingGroupId: String,
        val authorId: String,
        val dataReference: DataReference<String, DataRange?>,
        val permission: Permission,
        val users: Set<String>
)

data class SharingGroup(
        val id: String,
        val authorId: String,
        val permission: Permission = Permission.READ,
        val users: Set<String>,
        val data: DataReference<String, DataRange?>
)


