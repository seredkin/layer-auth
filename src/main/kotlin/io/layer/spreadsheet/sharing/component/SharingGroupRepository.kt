package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import io.layer.spreadsheet.sharing.component.SharingCommandResolver.Companion.referenceCache
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.stream.Stream
import javax.sql.DataSource
import kotlin.streams.toList

@Service
class SharingGroupRepository(private val dataSource: DataSource)
    : SharingQueryService<Stream<SharingGroup>>,
        SharingCommandService<AddPermissionCommand, DataReference<String, DataRange?>> {
    internal val db = { Database.connect(dataSource) }

    override fun fetchByDataReference(dataReference: DataReference<String, DataRange?>): Stream<SharingGroup> = transaction(db()) {
        val groupRefs = with(TDataReference) {
            select {
                fileId eq dataReference.fileId and
                        (sheetId eq dataReference.sheetId.let { UUID.fromString(it) }) and
                        (when {
                            dataReference.range?.cellRange != null -> (rangeCellsBetween eq dataReference.range?.cellRange.toString())
                            else -> (rangeCellsSet eq dataReference.range?.cellSet.toString())
                        })
            }.map { it[sharingGroupId] }.toList()

        }

        with(TSharingGroup) {
            select { id inList groupRefs }
        }.map { row -> rowToSharingGroup(row) }.stream()
    }

    override fun fetchByAuthorId(authorId: String): Stream<SharingGroup> {
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.authorId == authorId }
    }

    override fun fetchBySharingGroupId(sharingGroupId: String): Stream<SharingGroup> {
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.id == sharingGroupId }
    }

    override fun startSharing(pc: AddPermissionCommand) {
        transaction(db()) {
            val groupUuid = UUID.fromString(pc.sharingGroupId)
            with(TSharingGroup) {
                insert { sg ->
                    sg[id] = groupUuid
                    sg[authorId] = UUID.fromString(pc.authorId)
                    sg[permissionRead] = pc.permission.read
                    sg[permissionWrite] = pc.permission.write
                    sg[permissionShare] = pc.permission.share
                }
            }
            with(TDataReference) {
                insert { dr ->
                    dr[id] = UUID.randomUUID()
                    dr[sharingGroupId] = groupUuid
                    dr[fileId] = pc.dataReference.fileId
                    dr[sheetId] = pc.dataReference.sheetId?.let { UUID.fromString(it) }
                    dr[rangeCellsBetween] = when {
                        pc.dataReference.range?.cellRange != null -> pc.dataReference.range?.cellRange
                        else -> pc.dataReference.range?.cellSet
                    }.toString()
                }
            }
            pc.users.forEach { u ->
                with(TRelSharingGroupUsers) {
                    insert {
                        it[id] = UUID.randomUUID()
                        it[user_id] = UUID.fromString(u)
                        it[sharingGroupId] = UUID.fromString(pc.sharingGroupId)
                    }
                }
            }
        }
    }

    /**
     *     Removes all SharingGroups and DataReferences by provided DataReference
     */
    override fun stopSharing(dataReference: DataReference<String, DataRange?>): Unit = transaction(db()) {
        val groupRefs = fetchByDataReference(dataReference).map { UUID.fromString(it.id) }.toList()
        with(TSharingGroup) {
            deleteWhere { id inList groupRefs }
        }
    }
}