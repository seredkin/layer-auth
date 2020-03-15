package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore

data class DataFile(val id: String, val name: String, val authorId: String)
data class DataSheet(val id: String, @JsonIgnore val fileId: String)
data class DataCell(val x: Int, val y: Int, val sheetId: String)
data class DataRange(val cells: Set<DataCell> = setOf())
//TODO consider DataRange<Pair<DataCell, DataCell>>

sealed class Permission(
        val read: Boolean,
        val write: Boolean,
        val share: Boolean
) {
    private class ReadPermission : Permission(true, false, false)
    private class WritePermission : Permission(true, true, false)
    private class SharePermission : Permission(true, true, true)

    companion object {
        val SHARE: Permission = SharePermission()
        val WRITE: Permission = WritePermission()
        val READ: Permission = ReadPermission()
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

sealed class DataReference<S, out D>(
        val fileId: S,
        val sheetId: S?,
        val range: D?
) {
    class FileReference(fileId: String)
        : DataReference<String, Nothing>(fileId, null, null)

    class SheetReference(fileId: String, sheetId: String)
        : DataReference<String, Nothing>(fileId, sheetId, null)

    class RangeReference(fileId: String, sheetId: String, range: DataRange)
        : DataReference<String, DataRange>(fileId, sheetId, range)

    companion object {
        @JsonCreator
        @JvmStatic
        fun creator(fileId: String, sheetId: String?, range: DataRange?): DataReference<String, Any> = when {
            range == null && sheetId == null -> FileReference(fileId)
            range != null && sheetId != null -> RangeReference(fileId, sheetId, range)
            sheetId != null -> SheetReference(fileId, sheetId)
            else -> error("Range cannot be referenced without a sheetId: $fileId, $sheetId, $range")
        }
    }
}

data class AddPermissionCommand(
        val groupSharingId: String,
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


