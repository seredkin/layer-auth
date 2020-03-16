package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator

data class DataFile(val id: String, val name: String, val authorId: String)
data class DataSheet(val id: String, val fileId: String, val name: String)
data class DataCell(val x: Int, val y: Int, val sheetId: String)
data class DataRange(
        val cellSet: Set<DataCell> = setOf(),
        val cellRange: Set<DataCell> = setOf()
){
    companion object {
        @JsonCreator
        @JvmStatic
        fun creator(cellSet: Set<DataCell>?, cellRange: Set<DataCell>?): DataRange = when {
            cellSet != null && cellSet.isNotEmpty() -> DataRange(cellSet = cellSet)
            else -> DataRange(cellRange = cellRange!!)
        }
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
        fun creator(read: Boolean, write: Boolean, share: Boolean): Permission = when {
            share && write && read -> SHARE
            write && read -> WRITE
            read -> READ
            else -> error("Wrong permission values:$read, write:$write, share:$share")
        }
    }
}

private class ReadPermission(override val read: Boolean, override val write: Boolean, override val share: Boolean) : Permission
private class WritePermission(override val read: Boolean, override val write: Boolean, override val share: Boolean) : Permission
private class SharePermission(override val read: Boolean, override val write: Boolean, override val share: Boolean) : Permission

interface DataReference<S, out D> {
    val fileId: S
    val sheetId: S?
    val range: D?

    companion object {
        @JsonCreator
        @JvmStatic
        fun creator(fileId: String, sheetId: String?, range: DataRange?): DataReference<String, DataRange?> = when {
            range == null && sheetId == null -> FileReference(fileId)
            range != null && sheetId != null -> RangeReference(fileId, sheetId, range)
            sheetId != null -> SheetReference(fileId, sheetId)
            else -> error("Range cannot be referenced without a sheetId: $fileId, $sheetId, $range")
        }
    }

}

data class FileReference(override val fileId: String, override val sheetId: String? = null, override val range: DataRange? = null)
    : DataReference<String, DataRange>

data class SheetReference(override val fileId: String, override val sheetId: String, override val range: DataRange? = null)
    : DataReference<String, DataRange>

data class RangeReference(override val fileId: String, override val sheetId: String, override val range: DataRange)
    : DataReference<String,  DataRange>


data class AddPermissionCommand(
        val sharingGroupId: String,
        val authorId: String,
        val dataReference: DataReference<String, Any>,
        val permission: Permission,
        val users: Set<String>
)

data class SharingGroup(
        val id: String,
        val authorId: String,
        val permission: Permission = Permission.READ,
        val users: Set<String>,
        val data: Set<DataReference<String, Any>>
)


