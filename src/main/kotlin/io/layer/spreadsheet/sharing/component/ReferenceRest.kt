package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class ReferenceRest(private val userRepo: UserIdRepo, private val sheetIdRepo: SheetIdRepo) {
    @GetMapping("/ref/user/{userEmail}")
    fun getUserId(@PathVariable userEmail: String) = Mono.just(userRepo.getUserId(userEmail))

    @PostMapping("/ref/sheet")
    fun getSheetId(@RequestBody sr: SheetIdRequest): Mono<DataSheet> {
        val dataSheet = sheetIdRepo.fetchByFileIdAndSheetNameAndAuthor(sr.fileName, sr.sheetName, UUID.fromString(sr.authorId))
        return Mono.just(dataSheet)
    }

    data class SheetIdRequest(
            val fileName: String,
            val sheetName: String,
            val authorId: String
    )
}