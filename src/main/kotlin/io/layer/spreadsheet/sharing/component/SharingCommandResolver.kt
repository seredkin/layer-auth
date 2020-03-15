package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentSkipListMap

/**
 * In-memory implementation of a permission index
 * */
@Service
class SharingCommandResolver: SharingCommandService<AddPermissionCommand, DataReference<String, Any>> {
    //Any consistent hash would work as a key, we will use ordinary hashCode as a start
    companion object {
        internal val referenceCache = ConcurrentSkipListMap<Int, Set<SharingGroup>>()
    }

    override fun startSharing(pc: AddPermissionCommand) {
        val sharingGroup = SharingGroup(
                id = pc.groupSharingId,
                authorId = pc.authorId,
                permission = pc.permission,
                users = pc.users,
                data = setOf(pc.dataReference))
        referenceCache.merge(pc.dataReference.hashCode(), setOf(sharingGroup)){ t, u ->  t.plus(u)}
    }

    override fun stopSharing(dataReference: DataReference<String, Any>) {
        referenceCache.remove(dataReference.hashCode())
    }
}