package io.layer.spreadsheet.sharing.component

interface UserIdService {

    fun getUserId(userEmail: String): String
}
