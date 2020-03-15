package io.layer.spreadsheet.sharing.api

interface SharingQueryService<GroupStream>{
    fun fetchByDataReference(dataReference: DataReference<String, Any>): GroupStream
    fun fetchByAuthorId(authorId: String): GroupStream
}

interface SharingCommandService<Command, Reference>{
    fun startSharing(pc: Command)
    fun stopSharing(dataReference: Reference)
}

