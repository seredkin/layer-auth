package io.layer.spreadsheet.sharing.api

interface SharingQueryService<GroupStream> {
    fun byDataReference(dataReference: DataReference<String, DataRange?>): GroupStream
    fun byAuthorId(authorId: String): GroupStream
    fun bySharingGroupId(sharingGroupId: String): GroupStream
}