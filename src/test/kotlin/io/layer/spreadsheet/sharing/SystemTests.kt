package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.DataCell
import io.layer.spreadsheet.sharing.api.DataFile
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.FileReference
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.RangeReference
import io.layer.spreadsheet.sharing.component.SharingGroupRepository
import io.layer.spreadsheet.sharing.component.UserRepo
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.random.Random
import kotlin.streams.toList

@SpringBootTest(classes = [SharingMicroservice::class])
class SystemTests {

    private val id = { UUID.randomUUID().toString() }
    private val port = Random.nextInt(9_000, 10_000)
    @Autowired lateinit var sharingGroupRepository: SharingGroupRepository
    @Autowired lateinit var userIdRepo: UserRepo
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun jsonTest() {
        val mapper = ObjectMapper().findAndRegisterModules()
        val command = buildAddPermissionCommand()
        val json = mapper.writeValueAsString(command)
        println(json)
        val commandFromJson = mapper.readValue(json, StartSharingCommand::class.java)

        command shouldBeEqualTo commandFromJson
    }

    @Test
    fun cqrs() {
        val fileId = "cqrsResolversTest"
        val userId = userIdRepo.idByEmail("foo@bar.com")
        val participantId = userIdRepo.idByEmail("another@bar.com")
        val commandSvc = sharingGroupRepository
        val querySvc = sharingGroupRepository

        val file = DataFile(fileId, "testFile", userId)
        val dataReference = FileReference(fileId = file.id)
        val command = StartSharingCommand(id(), file.authorId, dataReference, Permission.SHARE, setOf(participantId))
        commandSvc.startSharing(command)
        val shares = querySvc.byDataReference(command.dataReference).toList()
        shares.map { it.data.fileId }.toList() shouldContain command.dataReference.fileId

        val byAuthorId = querySvc.byAuthorId(command.authorId).toList()
        byAuthorId.first().authorId shouldBeEqualTo command.authorId

        val bySharingGroupId = querySvc.bySharingGroupId(command.sharingGroupId).toList()
        bySharingGroupId.first().id shouldBeEqualTo command.sharingGroupId

        commandSvc.stopSharing(command.dataReference)

        querySvc.bySharingGroupId(command.sharingGroupId).toList().isEmpty() shouldBe true
        querySvc.byAuthorId(command.authorId).toList().isEmpty() shouldBe true
        querySvc.byDataReference(command.dataReference).toList().isEmpty() shouldBe true
    }



    private fun buildAddPermissionCommand(): StartSharingCommand {
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


}
