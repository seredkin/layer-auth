package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.id
import io.layer.spreadsheet.sharing.api.uuid
import io.layer.spreadsheet.sharing.component.RestPaths
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
private class FunctionalTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Test
    fun pingPong() {
        webClient.get().uri("${RestPaths.ping}").exchange().expectStatus().is2xxSuccessful
    }

    @Test
    fun addWebPermissionTest() {


        webClient.get().uri(RestPaths.ping).exchange().returnResult<String>().consumeWith { emptyFlux ->
            assert(emptyFlux.responseBody.blockLast()!! == "pong") { "Didn't get a pong response" }
        }

        val authorId =  uuid()
        val anotherUserId = id()
        val file = DataFile(id(), "testFile", authorId.toString())
        val sheet = DataSheet(fileId = file.id, name = "blank", authorId = authorId)
        val range = DataRange(setOf(DataCell(0, 0, sheet.id.toString()), DataCell(1, 0, sheet.id.toString())))
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id.toString(), range = range)

        val command = AddPermissionCommand(
                sharingGroupId = id(),
                authorId = authorId.toString(),
                dataReference = dataReference,
                permission = Permission.READ,
                users = setOf(anotherUserId)
        )


        webClient.post().uri(RestPaths.startSharing)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange().expectStatus().is2xxSuccessful

        webClient.post().uri(RestPaths.fetchByDataReference).bodyValue(dataReference).exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    groupFlux.responseBody.all { group ->
                        group.authorId == authorId.toString() && group.users.all(anotherUserId::equals)
                    }
                }

        webClient.post().uri(RestPaths.stopSharing)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command.dataReference)
                .exchange().expectStatus().is2xxSuccessful
    }

}