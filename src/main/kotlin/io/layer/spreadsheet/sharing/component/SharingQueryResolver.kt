package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.component.SharingCommandResolver.Companion.referenceCache
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import org.springframework.stereotype.Service
import java.util.stream.Stream

@Service
class SharingQueryResolver: SharingQueryService<Stream<SharingGroup>> {

    override fun fetchByDataReference(dataReference: DataReference<String, Any>): Stream<SharingGroup> {
        return referenceCache[dataReference.hashCode()]?.stream()?: Stream.empty()
    }

    override fun fetchByAuthorId(authorId: String): Stream<SharingGroup> {
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.authorId == authorId }
    }

    override fun fetchBySharingGroupId(sharingGroupId: String): Stream<SharingGroup>{
        return referenceCache.values.stream().flatMap { it.stream() }.filter { it.id == sharingGroupId }
    }
}