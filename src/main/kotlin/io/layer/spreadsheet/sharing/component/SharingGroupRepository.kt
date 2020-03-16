package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import io.layer.spreadsheet.sharing.component.SharingCommandResolver.Companion.referenceCache
import org.jetbrains.exposed.sql.Database
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.stream.Stream
import javax.sql.DataSource

@Service
@Profile("db")
class SharingGroupRepository(private val dataSource: DataSource)
    : SharingQueryService<Stream<SharingGroup>>,
        SharingCommandService<AddPermissionCommand, DataReference<String, Any>> {
    internal val db = { Database.connect(dataSource) }

    override fun fetchByDataReference(dataReference: DataReference<String, Any>): Stream<SharingGroup> {
        return referenceCache[dataReference.hashCode()]?.stream() ?: Stream.empty()
    }

    override fun fetchByAuthorId(authorId: String): Stream<SharingGroup> {
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.authorId == authorId }
    }

    override fun fetchBySharingGroupId(sharingGroupId: String): Stream<SharingGroup> {
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.id == sharingGroupId }
    }

    override fun startSharing(pc: AddPermissionCommand) {
        val sharingGroup = SharingGroup(
                id = pc.sharingGroupId,
                authorId = pc.authorId,
                permission = pc.permission,
                users = pc.users,
                data = setOf(pc.dataReference))
        referenceCache.merge(pc.dataReference.hashCode(), setOf(sharingGroup)) { t, u -> t.plus(u) }
    }

    override fun stopSharing(dataReference: DataReference<String, Any>) {
        referenceCache.remove(dataReference.hashCode())
    }
}