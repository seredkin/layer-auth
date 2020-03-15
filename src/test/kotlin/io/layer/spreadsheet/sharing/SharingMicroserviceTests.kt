package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.component.RestPaths
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.util.UUID
import kotlin.random.Random


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

        command.authorId shouldBeEqualTo commandFromJson.authorId
        command.users shouldBeEqualTo commandFromJson.users
        command.permission shouldBeEqualTo commandFromJson.permission
        //TODO Figure out sealed class DataReference.equals
        command.dataReference.fileId shouldBeEqualTo commandFromJson.dataReference.fileId
        command.dataReference.sheetId shouldBeEqualTo commandFromJson.dataReference.sheetId
        command.dataReference.range shouldBeEqualTo commandFromJson.dataReference.range

    }

    private fun buildAddPermissionCommand(): AddPermissionCommand {
        val anotherUserId = id()
        val file = DataFile(id(), "testFile", id())
        val sheet = DataSheet(id(), file.id)
        val range = DataRange(setOf(DataCell(0, 0, sheet.id), DataCell(1, 0, sheet.id)))
        val dataReference = DataReference.RangeReference(file.id, sheet.id, range)

        return AddPermissionCommand(id(), file.authorId, dataReference, Permission.WRITE, setOf(anotherUserId))
    }

    @Test
    fun contextLoads() {
        val ctx = SharingMicroservice.bootStrap(arrayOf("--server.port=$port"))
        val webClient = WebTestClient.bindToApplicationContext(ctx).build()

        webClient.get().uri(RestPaths.ping).exchange().returnResult<String>().consumeWith { emptyFlux ->
            assert(emptyFlux.responseBody.blockLast()!! == "pong") { "Didn't get a pong response" }
        }

        val authorId = id()
        val anotherUserId = id()
        val file = DataFile(id(), "testFile", authorId)
        val sheet = DataSheet(id(), file.id)
        val range = DataRange(setOf(DataCell(0, 0, sheet.id), DataCell(1, 0, sheet.id)))
        val dataReference = DataReference.RangeReference(file.id, sheet.id, range)

        val command = AddPermissionCommand(
                id(), authorId, dataReference, Permission.READ, setOf(anotherUserId)
        )
        webClient.post().uri(RestPaths.startSharing)
                .bodyValue(command)
                .exchange().expectStatus().is2xxSuccessful
        webClient.post().uri(RestPaths.fetchByDataReference).bodyValue(dataReference).exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    groupFlux.responseBody.all { group ->
                        group.authorId == authorId
                                && group.users.all(anotherUserId::equals)
                    }
                }
    }

}
