package io.layer.spreadsheet.sharing.component

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class UserIdResolver {
    private val id = { UUID.randomUUID().toString() }
    val cacheByUserId = ConcurrentHashMap<String, String>()

    fun getUserId(email: String): String {
        return cacheByUserId.merge(email, id()) { t, _ -> t }!!
    }
}