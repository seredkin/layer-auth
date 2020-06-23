package io.layer.spreadsheet.sharing.component

interface UserQueryService {
    fun idByEmail(userEmail: String): String
}
