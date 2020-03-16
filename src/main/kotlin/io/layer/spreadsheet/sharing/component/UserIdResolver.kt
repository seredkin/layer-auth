package io.layer.spreadsheet.sharing.component

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class UserIdResolver:UserIdService {
    private val id = { UUID.randomUUID().toString() }
    val cacheByUserId = ConcurrentHashMap<String, String>()

    override fun getUserId(userEmail: String): String {
        return cacheByUserId.merge(userEmail, id()) { t, _ -> t }!!
    }
}