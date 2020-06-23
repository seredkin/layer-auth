package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import io.layer.spreadsheet.sharing.api.SheetReference
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController("/")
class SharingRest(
        private val sharingCommandService: SharingGroupRepository,
        private val sharingQueryService: SharingGroupRepository
) : SharingQueryService<Flux<SharingGroup>>,
        SharingCommandService<StartSharingCommand, String> {

    @PostMapping(RestPaths.startSharing)
    override fun startSharing(@RequestBody pc: StartSharingCommand) {
        sharingCommandService.startSharing(pc)
    }

    @DeleteMapping("${RestPaths.stopSharing}/sheet/{sheetRef}")
    override fun stopSharing(@PathVariable sheetRef: String) {
        sharingCommandService.stopSharing(SheetReference(sheetId = sheetRef, fileId = ""))
    }

    @PostMapping(RestPaths.fetchByDataReference)
    override fun byDataReference(@RequestBody dataReference: DataReference<String, DataRange?>): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.byDataReference(dataReference))
    }

    @GetMapping(RestPaths.fetchByAuthorId + "/{authorId}")
    override fun byAuthorId(@PathVariable authorId: String): Flux<SharingGroup> =
            Flux.fromStream(sharingQueryService.byAuthorId(authorId))

    @GetMapping(RestPaths.fetchBySharingGroupId + "/{sharingGroupId}")
    override fun bySharingGroupId(@PathVariable sharingGroupId: String): Flux<SharingGroup> =
            Flux.fromStream(sharingQueryService.bySharingGroupId(sharingGroupId))
}

