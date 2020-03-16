package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.component.SheetIdResolver
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import java.util.UUID

class SheetIdResolverTest {

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
        val sheetIdResolver = SheetIdResolver()

        val fileId = UUID.randomUUID().toString()

        //Gen ID for each DataSheet
        val dataSheets = names.map { sheetName ->
            sheetIdResolver.getDataSheet(fileId, sheetName)
        }.toList()

        //No duplications
        dataSheets.toSet().size shouldBe names.size

        //Ensure each DataSheet is searchable
        names.forEach { sheetName ->
            val dataSheetAndReference = sheetIdResolver.fetchByFileIdAndSheetName(fileId, sheetName)

            dataSheets shouldContain dataSheetAndReference.first
        }


    }

}