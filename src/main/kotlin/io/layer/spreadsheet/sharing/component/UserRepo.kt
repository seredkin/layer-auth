package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.UserAddCommand
import io.layer.spreadsheet.sharing.api.UserCommandService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*
import java.util.Optional.ofNullable
import javax.sql.DataSource

@Component
@Primary
class UserRepo(private val dataSource: DataSource) : UserQueryService, UserCommandService {
    private val db = { Database.connect(dataSource) }

    override fun idByEmail(userEmail: String): String = transaction(db()) {

        with(TUserEmail) {
            select { email eq userEmail }.single()[id].toString()
        }
    }

    override fun addUser(userAddCommand: UserAddCommand) {
        transaction(db()) {
            with(TUserEmail) {
                insert { u ->
                    u[id] = ofNullable(userAddCommand.id).map { UUID.fromString(it) }.orElseGet { UUID.randomUUID() }
                    u[email] = userAddCommand.email
                }
            }
        }
    }

}