package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.*

class JsonApiTests {

    private val id = { UUID.randomUUID().toString() }
    private val mapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun cellSetJsonTest() {
        val command = buildCellSetSharginCommand()
        val json = mapper.writeValueAsString(command)
        println(json)
        val commandFromJson = mapper.readValue(json, StartSharingCommand::class.java)

        command shouldBeEqualTo commandFromJson
    }

    @Test
    fun cellRangeJsonTest() {
        val command = buildCellRangeSharingCommand()
        val json = mapper.writeValueAsString(command)
        println(json)
        val commandFromJson = mapper.readValue(json, StartSharingCommand::class.java)

        command shouldBeEqualTo commandFromJson
    }

    @Test
    fun sheetJsonTest() {
        val command = buildSheetSharingCommand()
        val json = mapper.writeValueAsString(command)
        println(json)
        val commandFromJson = mapper.readValue(json, StartSharingCommand::class.java)

        command shouldBeEqualTo commandFromJson
    }

    private fun buildCellSetSharginCommand(): StartSharingCommand {
        val anotherUserId = id()
        val file = DataFile(id = id(), name = "testFile", authorId = id())
        val sheet = DataSheet(fileId = file.id, name = "blank", authorId = UUID.randomUUID())
        val range = DataRange(cellSet = setOf(DataCell(0, 0, sheet.id.toString()), DataCell(1, 0, sheet.id.toString())))
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id.toString(), range = range)

        return StartSharingCommand(
                sharingGroupId = id(),
                authorId = file.authorId,
                dataReference = dataReference,
                permission = Permission.WRITE,
                users = setOf(anotherUserId)
        )
    }

    private fun buildCellRangeSharingCommand(): StartSharingCommand {
        val anotherUserId = id()
        val authorId = uuid()
        val file = DataFile(id = id(), name = "testFile", authorId = "$authorId")
        val sheet = DataSheet(fileId = file.id, name = "blank", authorId = authorId, id = uuid())
        val range = DataRange(cellRange = setOf(DataCell(1, 1, sheet.id.toString()), DataCell(3, 3, sheet.id.toString())))
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id.toString(), range = range)

        return StartSharingCommand(
                sharingGroupId = id(),
                authorId = file.authorId,
                dataReference = dataReference,
                permission = Permission.WRITE,
                users = setOf(anotherUserId)
        )
    }

    private fun buildSheetSharingCommand(): StartSharingCommand {
        val anotherUserId = id()
        val file = DataFile(id = id(), name = "testFile", authorId = id())
        val sheet = DataSheet(fileId = file.id, name = "blank", authorId = UUID.randomUUID())
        val dataReference = SheetReference(fileId = file.id, sheetId = sheet.id.toString())

        return StartSharingCommand(
                sharingGroupId = id(),
                authorId = file.authorId,
                dataReference = dataReference,
                permission = Permission.WRITE,
                users = setOf(anotherUserId)
        )
    }


}
