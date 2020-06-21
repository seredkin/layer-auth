package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID
import javax.sql.DataSource

@Component
class SheetIdRepo(private val dataSource: DataSource) : SheetIdService {
    internal val db = { Database.connect(dataSource) }

    override fun fetchByFileIdAndSheetNameAndAuthor(fileId: String, sheetName: String, authorId: UUID): DataSheet = transaction(db()) {
        Optional.ofNullable(with(TSheetName) {
            select { TSheetName.fileId eq fileId and (TSheetName.name eq sheetName) }.singleOrNull()
        }).map { rowToDataSheet(it) }.orElseGet {
            val ds = DataSheet(fileId = fileId, name = sheetName, authorId = authorId)
            with(TSheetName) {
                insert {
                    it[this@with.id] = ds.id
                    it[this@with.authorId] = authorId
                    it[this@with.fileId] = fileId
                    it[this@with.name] = sheetName
                }
                ds
            }
        }
    }
}