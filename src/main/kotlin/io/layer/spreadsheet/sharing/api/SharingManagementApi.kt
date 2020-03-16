package io.layer.spreadsheet.sharing.api

import java.util.stream.Stream

interface SharingQueryService<GroupStream>{
    fun fetchByDataReference(dataReference: DataReference<String, DataRange?>): GroupStream
    fun fetchByAuthorId(authorId: String): GroupStream
    fun fetchBySharingGroupId(sharingGroupId: String): Stream<SharingGroup>
}

interface SharingCommandService<Command, Reference>{
    fun startSharing(pc: Command)
    fun stopSharing(dataReference: Reference)
}

