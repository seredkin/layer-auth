package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.component.SheetIdResolver
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class SheetNameTest {

    private val samples = arrayOf(
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

        val ids = samples.map {
            sheetIdResolver.getSheetId(it, fileId)
        }.toList()

        ids.toSet().size shouldBe samples.size

    }

}