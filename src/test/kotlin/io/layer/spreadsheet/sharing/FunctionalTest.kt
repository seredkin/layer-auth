package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.UserAddCommand
import io.layer.spreadsheet.sharing.api.id
import io.layer.spreadsheet.sharing.component.RestPaths
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
private class FunctionalTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Test
    fun startStopSharing() {


        //Create Author User
        val authorId = id()
        webClient.post().uri(RestPaths.user)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserAddCommand("test@$authorId.test", authorId))
                .exchange().expectStatus().is2xxSuccessful

        //Create a collaborating User
        val anotherUserId = id()
        webClient.post().uri(RestPaths.user)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserAddCommand("test@$anotherUserId.test", anotherUserId))
                .exchange().expectStatus().is2xxSuccessful

        //Create data reference (SheetAddCommand)
        val dataSheetId = id()
        val sheet = webClient.put().uri(RestPaths.sheet)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SheetAddCommand(
                        id = dataSheetId,
                        fileName = "$authorId:testFile",
                        sheetName = "$authorId:blank",
                        authorId = authorId
                ))
                .exchange().expectStatus().is2xxSuccessful.returnResult(DataSheet::class.java).responseBody.blockFirst()!!

        //Create a Range on an existing Sheet
        val range = DataRange(setOf(DataCell(0, 0, sheet.id.toString()), DataCell(1, 0, sheet.id.toString())))
        val rangeReference = RangeReference(fileId = "file:${id()}", sheetId = dataSheetId, range = range)

        val sharingGroupId = id()
        val command = StartSharingCommand(
                sharingGroupId = sharingGroupId,
                authorId = authorId,
                dataReference = rangeReference,
                permission = Permission.READ,
                users = setOf(anotherUserId)
        )


        //Start sharing File/Sheet/Range reference
        webClient.post().uri(RestPaths.startSharing)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange().expectStatus().is2xxSuccessful

        // Ensure the dataRef is shared with given users
        webClient.post().uri(RestPaths.fetchByDataReference).bodyValue(rangeReference).exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    groupFlux.responseBody.all { group ->
                        group.authorId == authorId.toString() && group.users.all(anotherUserId::equals)
                    }
                }
        // Ensure nothing is shared
        webClient.get().uri("${RestPaths.fetchByAuthorId}/${command.authorId}").exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    val block = groupFlux.responseBody.collectList().block()!!
                    assertEquals(
                            block.size, 1
                    )
                }

        // Stop sharing the Sheet
        //TODO stop sharing a Range
        webClient.delete().uri("${RestPaths.stopSharing}/sheet/$dataSheetId")
                .exchange().expectStatus().is2xxSuccessful

        // Ensure nothing is shared
        webClient.get().uri("${RestPaths.fetchByAuthorId}/$authorId").exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    assertTrue {
                        groupFlux.responseBody.collectList().block()!!.isEmpty()
                    }
                }

        // Ensure nothing is shared
        webClient.get().uri("${RestPaths.fetchBySharingGroupId}/$sharingGroupId").exchange()
                .returnResult<SharingGroup>().consumeWith { groupFlux ->
                    assertTrue {
                        groupFlux.responseBody.collectList().block()!!.isEmpty()
                    }
                }

    }

}