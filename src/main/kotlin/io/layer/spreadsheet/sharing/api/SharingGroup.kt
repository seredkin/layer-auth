package io.layer.spreadsheet.sharing.api

data class SharingGroup(
        val id: String,
        val authorId: String,
        val permission: Permission,
        val users: Set<String>,
        val data: DataReference<String, DataRange?>
)
