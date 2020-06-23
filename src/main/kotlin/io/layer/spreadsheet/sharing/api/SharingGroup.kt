package io.layer.spreadsheet.sharing.api

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.Permission

data class SharingGroup(
        val id: String,
        val authorId: String,
        val permission: Permission = Permission.READ,
        val users: Set<String>,
        val data: DataReference<String, DataRange?>
)
