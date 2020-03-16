package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
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

    override fun getDataSheet(fileId: String, sheetName: String, authorId: UUID): DataSheet = transaction(db()) {
        val resultRow: ResultRow? = with(TSheetName) {
            select { TSheetName.fileId eq fileId and (TSheetName.name eq sheetName) }.singleOrNull()
        }
        when {
            resultRow != null -> rowToDataSheet(resultRow)
            else -> {
                val sheetId = UUID.randomUUID()
                TSheetName.insert {
                    it[id] = sheetId
                    it[name] = sheetName
                    it[this.fileId] = fileId
                    it[this.authorId] = authorId
                }
                DataSheet(id = sheetId.toString(), fileId = fileId, name = sheetName, authorId = authorId)
            }
        }
    }

    override fun fetchByFileIdAndSheetName(fileId: String, sheetName: String): Optional<DataSheet> = transaction(db()) {
        Optional.ofNullable(with(TSheetName) {
            select { TSheetName.fileId eq fileId and (TSheetName.name eq sheetName) }.singleOrNull()
        }).map { rowToDataSheet(it) }
    }
}