package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.FileReference
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SheetReference
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.UUID

internal interface SharingDomainTable {
    val id: Column<UUID>
    val createdAt: Column<DateTime>
}

internal object TUserEmail : Table("user_email"), SharingDomainTable {
    override val id = uuid("id").primaryKey().clientDefault { UUID.randomUUID() }
    val email = text("email").index()
    override val createdAt = datetime("created_at")
}

internal object TSheetName : Table("sheet_name"), SharingDomainTable {
    override val id = uuid("id").primaryKey()
    val authorId = uuid("author_id").references(TUserEmail.id)
    val fileId = text("file_id")
    val name = text("name").index()
    override val createdAt = datetime("created_at")
}

internal object TSharingGroup : Table("sharing_group"), SharingDomainTable {
    override val id = uuid("id").primaryKey()
    val authorId = uuid("author_id").references(TUserEmail.id)
    val permissionRead = bool("permission_read").default(true)
    val permissionWrite = bool("permission_write").default(false)
    val permissionShare = bool("permission_share").default(false)
    override val createdAt = datetime("created_at")
}

internal object TDataReference : Table("data_reference"), SharingDomainTable {
    override val id = uuid("id").primaryKey()
    val sharingGroupId = uuid("sharing_group_id").references(TSharingGroup.id)
    val sheetId = uuid("sheet_id").references(TSheetName.id).nullable()
    val fileId = text("file_id")
    val rangeCellsSet = text("range_cells_set").nullable()
    val rangeCellsBetween = text("range_cells_between").nullable()
    override val createdAt = datetime("created_at")
}

internal object TRelSharingGroupUsers : Table("rel_sharing_group_users"), SharingDomainTable {
    override val id = uuid("id").primaryKey()
    val sharingGroupId = uuid("sharing_group_id").references(TSharingGroup.id)
    val user_id = uuid("user_id").references(TUserEmail.id)
    override val createdAt = datetime("created_at")
}

internal fun rowToDataSheet(it: ResultRow): DataSheet {
    return DataSheet(
            id = it[TSheetName.id],
            fileId = it[TSheetName.fileId].toString(),
            name = it[TSheetName.name],
            authorId = it[TSheetName.authorId]
    )
}

internal fun rowToSharingGroup(it: ResultRow, dataReference: DataReference<String, DataRange?>): SharingGroup {
    return with(TSharingGroup) {
        SharingGroup(
                id = it[id].toString(),
                authorId = it[authorId].toString(),
                permission = Permission.of(read = it[permissionRead], write = it[permissionWrite], share = it[permissionShare]),
                users = setOf(),
                data = dataReference
        )
    }
}

internal fun rowToDataReference(row: ResultRow): DataReference<String, DataRange?> {
    with(TDataReference) {
        return when {
            row[sheetId] == null -> FileReference(id = row[id].toString(), fileId = row[fileId])
            row[rangeCellsSet] == null && row[rangeCellsBetween] == null -> SheetReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString()
            )
            row[rangeCellsSet] != null -> RangeReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString(),
                    range = DataRange.fromStringSet(row[rangeCellsSet]!!)
            )
            else -> RangeReference(
                    id = row[id].toString(),
                    fileId = row[fileId],
                    sheetId = row[sheetId].toString(),
                    range = DataRange.fromStringSet(row[rangeCellsBetween]!!)
            )
        }

    }
}