package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.UserAddCommand
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
        webClient.get().uri(RestPaths.ping).exchange().expectStatus().is2xxSuccessful
    }

    @Test
    fun addWebPermissionTest() {

        val authorId = uuid()
        val anotherUserId = uuid().toString()
        val file = DataFile(id(), "$authorId:testFile", "$authorId")

        //Create Author User
        webClient.post().uri(RestPaths.user)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserAddCommand("test@$authorId.test", "$authorId"))
                .exchange().expectStatus().is2xxSuccessful

        //Create a collaborating User
        webClient.post().uri(RestPaths.user)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserAddCommand("test@$anotherUserId.test", anotherUserId))
                .exchange().expectStatus().is2xxSuccessful

        //Create data reference (SheetAddCommand)
        val sheet = webClient.put().uri(RestPaths.sheet)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SheetAddCommand(
                        fileName = file.name,
                        sheetName = "$authorId:blank",
                        authorId = authorId.toString()
                ))
                .exchange().expectStatus().is2xxSuccessful.returnResult(DataSheet::class.java).responseBody.blockFirst()!!

        //Create a Range on an existing Sheet
        val range = DataRange(setOf(DataCell(0, 0, sheet.id.toString()), DataCell(1, 0, sheet.id.toString())))
        val dataReference = RangeReference(fileId = file.id, sheetId = sheet.id.toString(), range = range)

        //Start sharing File/Sheet/Range reference
        val command = StartSharingCommand(
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