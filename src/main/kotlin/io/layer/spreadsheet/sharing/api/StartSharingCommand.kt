package io.layer.spreadsheet.sharing.api

/** Rest command model for starting the sharing of a data
 * @see io.layer.spreadsheet.sharing.api.SharingCommandService.startSharing
 * */
data class StartSharingCommand(
        val sharingGroupId: String,
        val authorId: String,
        val dataReference: DataReference<String, DataRange?>,
        val permission: Permission,
        val users: Set<String>
)
