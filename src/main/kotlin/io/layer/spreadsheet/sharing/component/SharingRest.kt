package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.StartSharingCommand
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController("/")
class SharingRest(
        private val sharingCommandService: SharingGroupRepository,
        private val sharingQueryService: SharingGroupRepository
) : SharingQueryService<Flux<SharingGroup>>,
        SharingCommandService<StartSharingCommand, DataReference<String, DataRange?>> {

    @PostMapping(RestPaths.startSharing)
    override fun startSharing(@RequestBody pc: StartSharingCommand) {
        sharingCommandService.startSharing(pc)
    }

    @PostMapping(RestPaths.stopSharing)
    override fun stopSharing(@RequestBody dataReference: DataReference<String, DataRange?>) {
        sharingCommandService.stopSharing(dataReference)
    }

    @PostMapping(RestPaths.fetchByDataReference)
    override fun byDataReference(@RequestBody dataReference: DataReference<String, DataRange?>): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.byDataReference(dataReference))
    }

    @GetMapping(RestPaths.fetchByAuthorId)
    override fun byAuthorId(@PathVariable authorId: String): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.byAuthorId(authorId))
    }

    @GetMapping(RestPaths.ping)
    fun pingPong() = Mono.just("pong")

    override fun bySharingGroupId(sharingGroupId: String): Flux<SharingGroup> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

