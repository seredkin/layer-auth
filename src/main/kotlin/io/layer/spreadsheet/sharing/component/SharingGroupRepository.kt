package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
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
        SharingCommandService<StartSharingCommand, DataReference<String, DataRange?>> {
    internal val db = { Database.connect(dataSource) }

    /**
     * This function CASCADE selects DataReferences from a given level. E.g. FILE selects FILE, its SHEETS and RANGES
     * */
    override fun byDataReference(dataReference: DataReference<String, DataRange?>): Stream<SharingGroup> = transaction(db()) {
        val dataReferenceList = with(TDataReference) {
            when (dataReference.type()) {
                DataReference.TYPE_FILE ->
                    select { fileId eq dataReference.fileId }
                DataReference.TYPE_SHEET ->
                    select { sheetId eq UUID.fromString(dataReference.sheetId) }
                DataReference.TYPE_CELL_SET ->
                    select {
                        fileId eq dataReference.fileId and
                                (sheetId eq UUID.fromString(dataReference.sheetId)) and
                                (rangeCellsSet eq DataRange.asStringSet(dataReference.range!!))
                    }
                else -> //As Range Between
                    select {
                        fileId eq dataReference.fileId and
                                (sheetId eq UUID.fromString(dataReference.sheetId)) and
                                (rangeCellsSet eq DataRange.asStringBetween(dataReference.range!!))
                    }
            }
        }.map { row -> Pair(row[TDataReference.sharingGroupId], rowToDataReference(row)) }.toMap()

        with(TSharingGroup) {
            select { id inList dataReferenceList.keys }
        }.map { row ->
            val dataRefId = row[TSharingGroup.id]
            rowToSharingGroup(row, dataReferenceList[dataRefId] ?: error("ID not mapped: $dataRefId"))
        }.stream()


    }

    override fun byAuthorId(authorId: String): Stream<SharingGroup> = transaction(db()) {
        (TSharingGroup innerJoin TDataReference)
                .select { TSharingGroup.authorId eq UUID.fromString(authorId) }
                .map { row -> rowToSharingGroup(row, rowToDataReference(row)) }.stream()
    }

    override fun bySharingGroupId(sharingGroupId: String): Stream<SharingGroup> = transaction(db()) {
        (TSharingGroup innerJoin TDataReference)
                .select { TSharingGroup.id eq UUID.fromString(sharingGroupId) }
                .map { rowToSharingGroup(it, rowToDataReference(it)) }.stream()
    }

    override fun startSharing(pc: StartSharingCommand) {
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
            }.also {
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
            }.also {
                pc.users.forEach { userId ->
                    with(TRelSharingGroupUsers) {
                        insert { uRelG ->
                            uRelG[id] = UUID.randomUUID()
                            uRelG[user_id] = UUID.fromString(userId)
                            uRelG[sharingGroupId] = UUID.fromString(pc.sharingGroupId)
                        }
                    }
                }
            }
        }
    }

    /**
     *     Removes all SharingGroups and DataReferences by provided DataReference
     */
    override fun stopSharing(dataReference: DataReference<String, DataRange?>): Unit = transaction(db()) {
        val groupRefs = byDataReference(dataReference).map { UUID.fromString(it.id) }.toList()
        with(TSharingGroup) {
            deleteWhere { id inList groupRefs }
        }
    }
}