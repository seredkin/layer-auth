package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.component.SharingCommandResolver
import io.layer.spreadsheet.sharing.component.SharingQueryResolver
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.random.Random
import kotlin.streams.toList


class SharingMicroserviceTests {

    private val id = { UUID.randomUUID().toString() }
    private val port = Random.nextInt(9_000, 10_000)


    @Test
    fun jsonTest() {
        val mapper = ObjectMapper().findAndRegisterModules()
        val command = buildAddPermissionCommand()
        val json = mapper.writeValueAsString(command)
        println(json)
        val commandFromJson = mapper.readValue(json, AddPermissionCommand::class.java)

        command shouldBeEqualTo commandFromJson
    }

    @Test
    fun cqrsResolversTest() {
        val commandSvc = SharingCommandResolver()
        val querySvc = SharingQueryResolver()

        val command = buildAddPermissionCommand()
        commandSvc.startSharing(command)
        val shares = querySvc.fetchByDataReference(command.dataReference).toList()
        shares.first().data.first() shouldBe command.dataReference

        val byAuthorId = querySvc.fetchByAuthorId(command.authorId).toList()
        byAuthorId.first().authorId shouldBeEqualTo command.authorId

        val bySharingGroupId = querySvc.fetchBySharingGroupId(command.sharingGroupId).toList()
        bySharingGroupId.first().id shouldBeEqualTo command.sharingGroupId

        commandSvc.stopSharing(command.dataReference)

        querySvc.fetchBySharingGroupId(command.sharingGroupId).toList().isEmpty() shouldBe true
        querySvc.fetchByAuthorId(command.authorId).toList().isEmpty() shouldBe true
        querySvc.fetchByDataReference(command.dataReference).toList().isEmpty() shouldBe true
    }

    private fun buildAddPermissionCommand(): AddPermissionCommand {
        val anotherUserId = id()
        val file = DataFile(id(), "testFile", id())
        val sheet = DataSheet(id(), file.id, "blank")
        val range = DataRange(cellSet = setOf(DataCell(0, 0, sheet.id), DataCell(1, 0, sheet.id)))
        val dataReference = RangeReference(file.id, sheet.id, range)

        return AddPermissionCommand(id(), file.authorId, dataReference, Permission.WRITE, setOf(anotherUserId))
    }
}
