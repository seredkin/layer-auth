package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.SharingMicroservice
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.SheetQuery
import io.layer.spreadsheet.sharing.api.UserAddCommand
import io.layer.spreadsheet.sharing.api.id
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest(classes = [SharingMicroservice::class])
class SheetQueryResolverTest {

    @Autowired
    lateinit var sheetRepo: SheetRepo

    @Autowired
    lateinit var userIdRepo: UserRepo

    private val names = hashSetOf(
            "SheetNameWithoutSpacesSingleCell!A1",
            "'Sheet Name With Spaces Single Cell'!B4",
            "OnlyTheSheetNameWithoutQuotes",
            "'SheetNameWithQuotesNoSpaces'",
            "'Sheet Name With Quotes And Spaces'",
            "'Long Sheet Name With Spaces and Range'!B3:B5",
            "SheetNameWithoutSpacesWithRanges!B2:B5"
    )

    @Test
    fun testValidationAndIdGeneration() {

        val testId = id()
        val fileId = "test_fileId $testId"
        val userEmail = "my@$testId.email.com"
        userIdRepo.addUser(UserAddCommand(email = userEmail))
        val authorId = UUID.fromString(userIdRepo.idByEmail(userEmail))


        //Save each DataSheet
        val dataSheets = names.map { sheetName ->
            sheetRepo.addDataSheet(SheetAddCommand(fileName = fileId, sheetName = sheetName, authorId = authorId.toString()))
        }.toList()

        //Ensure no duplications
        dataSheets.toSet().size shouldBe names.size

        //Ensure each DataSheet is searchable
        dataSheets.forEach {
            val dataSheetAndReference = sheetRepo.byFileIdAndSheetNameAndAuthor(
                    SheetQuery(
                            fileName = it.fileId,
                            sheetName = it.name,
                            authorId = it.authorId.toString()
                    )
            )
            assertTrue(names.contains(dataSheetAndReference.name), "Name is searchable")
        }

    }

}