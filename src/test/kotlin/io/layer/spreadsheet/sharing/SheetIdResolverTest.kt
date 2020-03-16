package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.component.SheetIdRepo
import io.layer.spreadsheet.sharing.component.UserIdRepo
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(classes = [SharingMicroservice::class])
class SheetIdResolverTest {

    @Autowired
    lateinit var sheetIdResolver: SheetIdRepo
    @Autowired
    lateinit var userIdRepo: UserIdRepo

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

        val fileId = "test_fileId ${UUID.randomUUID()}"
        val authorId = UUID.fromString(userIdRepo.getUserId("my@email.com"))


        //Gen ID for each DataSheet
        val dataSheets = names.map { sheetName ->
            sheetIdResolver.getDataSheet(fileId, sheetName, authorId)
        }.toList()

        //No duplications
        dataSheets.toSet().size shouldBe names.size

        //Ensure each DataSheet is searchable
        names.forEach { sheetName ->
            val dataSheetAndReference = sheetIdResolver.fetchByFileIdAndSheetName(fileId, sheetName)

            dataSheets shouldContain dataSheetAndReference.get()
        }

    }

}