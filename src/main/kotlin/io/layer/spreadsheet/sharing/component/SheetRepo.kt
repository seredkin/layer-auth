package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.SheetCommandService
import io.layer.spreadsheet.sharing.api.SheetQuery
import io.layer.spreadsheet.sharing.api.SheetQueryResolver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component
import java.util.*
import javax.sql.DataSource

@Component
class SheetRepo(private val dataSource: DataSource) : SheetQueryResolver, SheetCommandService {
    private val db = { Database.connect(dataSource) }

    override fun byFileIdAndSheetNameAndAuthor(sheetQuery: SheetQuery): DataSheet = transaction(db()) {
        with(TSheetName) {
            rowToDataSheet(
                    select {
                        this@with.fileId eq sheetQuery.fileName and (name eq sheetQuery.sheetName) and (authorId eq UUID.fromString(sheetQuery.authorId))
                    }.single())
        }
    }

    override fun addDataSheet(sheetAddCommand: SheetAddCommand): DataSheet = transaction(db()) {
        val sheetUuid = UUID.randomUUID()
        val authorUuid = UUID.fromString(sheetAddCommand.authorId)
        with(TSheetName) {
            insert { row ->
                row[authorId] = authorUuid
                row[fileId] = sheetAddCommand.fileName
                row[name] = sheetAddCommand.sheetName
                row[id] = sheetUuid
            }
        }
        DataSheet(
                id = sheetUuid,
                fileId = sheetAddCommand.fileName,
                name = sheetAddCommand.sheetName,
                authorId = authorUuid
        )
    }
}