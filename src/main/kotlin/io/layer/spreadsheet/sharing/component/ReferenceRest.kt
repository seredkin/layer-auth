package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataSheet
import io.layer.spreadsheet.sharing.api.SheetAddCommand
import io.layer.spreadsheet.sharing.api.SheetQuery
import io.layer.spreadsheet.sharing.api.UserAddCommand
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ReferenceRest(private val userRepo: UserRepo, private val sheetIdRepo: SheetRepo) {
    @GetMapping("${RestPaths.user}/{userEmail}")
    fun getUserId(@PathVariable userEmail: String) = Mono.fromCallable { userRepo.idByEmail(userEmail) }

    @PostMapping(RestPaths.user)
    fun addUser(@RequestBody cmdUserAdd: UserAddCommand) {
        userRepo.addUser(cmdUserAdd)
    }

    @PostMapping(RestPaths.sheet)
    fun querySheet(@RequestBody sr: SheetQuery): DataSheet {
        return sheetIdRepo.byFileIdAndSheetNameAndAuthor(sr)
    }

    @PutMapping(RestPaths.sheet)
    fun addSheet(@RequestBody sr: SheetAddCommand): DataSheet {
        return sheetIdRepo.addDataSheet(sr)
    }


}

