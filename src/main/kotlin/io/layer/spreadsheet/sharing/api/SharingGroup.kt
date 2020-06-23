package io.layer.spreadsheet.sharing.api

data class SharingGroup(
        val id: String,
        val authorId: String,
        val permission: Permission = Permission.READ,
        val users: Set<String>,
        val data: DataReference<String, DataRange?>
)
