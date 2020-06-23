package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.SharingMicroservice
import io.layer.spreadsheet.sharing.api.Permission
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.SheetReference
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.UserAddCommand
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import kotlin.random.Random
import kotlin.streams.toList

@SpringBootTest(classes = [SharingMicroservice::class])
class DataApiTests {

    private val id = { UUID.randomUUID().toString() }
    private val port = Random.nextInt(9_000, 10_000)

    @Autowired
    lateinit var sharingGroupRepository: SharingGroupRepository

    @Autowired
    lateinit var userIdRepo: UserRepo

    @Autowired
    lateinit var sheetRepo: SheetRepo
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun cqrsDataTest() {
        val testId = "testFile:${id()}"
        val fileId = "cqrsResolversTest:$testId"

        val authorId = id()
        userIdRepo.addUser(UserAddCommand(email = "author@$testId.com", id = authorId))

        val participantId = UUID.randomUUID()
        userIdRepo.addUser(UserAddCommand(email = "another@$testId.com", id = participantId.toString()))

        // TODO Split up with real CQRS
        val commandSvc = sharingGroupRepository
        val querySvc = sharingGroupRepository

        val dataSheetId = id()
        sheetRepo.addDataSheet(SheetAddCommand(
                id = dataSheetId,
                fileName = fileId,
                authorId = authorId,
                sheetName = "sheetName"
        ))

        val dataReference = SheetReference(fileId = fileId, sheetId = dataSheetId)
        val command = StartSharingCommand(
                sharingGroupId = id(),
                authorId = authorId,
                dataReference = dataReference,
                permission = Permission.SHARE,
                users = setOf(participantId.toString())
        )
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

}
