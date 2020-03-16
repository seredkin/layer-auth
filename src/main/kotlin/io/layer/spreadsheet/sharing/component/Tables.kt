package io.layer.spreadsheet.sharing.component

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.UUID

internal interface SharingDomainTable {
    val id: Column<UUID>
    val createdAt: Column<DateTime>
}

internal object TUserEmail : Table("user_email"), SharingDomainTable {
    override val id = uuid("id").primaryKey()
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
    val authorId = TSheetName.uuid("author_id").references(TUserEmail.id)
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
