package io.layer.spreadsheet.sharing.component

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID
import javax.sql.DataSource

@Component
@Primary
class UserIdRepo(private val dataSource: DataSource) : UserIdService {
    internal val db = { Database.connect(dataSource) }

    private val id = { UUID.randomUUID() }

    override fun getUserId(userEmail: String): String = transaction(db()) {

        val uuid: UUID? = with(TUserEmail) {
            select { email eq userEmail }.singleOrNull()?.get(id)
        }
        when {
            uuid != null -> uuid.toString()
            else -> {
                val userId = id()
                TUserEmail.insert {
                    it[id] = userId
                    it[email] = userEmail
                }
                userId.toString()
            }
        }
    }

}