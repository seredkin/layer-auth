package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.component.RestPaths
import io.layer.spreadsheet.sharing.component.SharingCommandResolver
import io.layer.spreadsheet.sharing.component.SharingQueryResolver
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.util.UUID
import kotlin.random.Random
import kotlin.streams.toList


class SystemTests {

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
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id, range = range)

        return AddPermissionCommand(id(), file.authorId, dataReference, Permission.WRITE, setOf(anotherUserId))
    }

    fun contextLoads() {
        val ctx = SharingMicroservice.bootStrap(arrayOf("--server.port=$port"))
        val webClient = WebTestClient.bindToApplicationContext(ctx).build()

        webClient.get().uri(RestPaths.ping).exchange().returnResult<String>().consumeWith { emptyFlux ->
            assert(emptyFlux.responseBody.blockLast()!! == "pong") { "Didn't get a pong response" }
        }

        val authorId = id()
        val anotherUserId = id()
        val file = DataFile(id(), "testFile", authorId)
        val sheet = DataSheet(id(), file.id, "blank")
        val range = DataRange(setOf(DataCell(0, 0, sheet.id), DataCell(1, 0, sheet.id)))
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id, range = range)

        val command = AddPermissionCommand(id(), authorId, dataReference, Permission.READ, setOf(anotherUserId))

        webClient.post().uri(RestPaths.startSharing)
                .bodyValue(command)
                .exchange().expectStatus().is2xxSuccessful

        webClient.post().uri(RestPaths.fetchByDataReference).bodyValue(dataReference).exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    groupFlux.responseBody.all { group ->
                        group.authorId == authorId && group.users.all(anotherUserId::equals)
                    }
                }
    }

}
