package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.AddPermissionCommand
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
        private val sharingCommandService: SharingCommandResolver,
        private val sharingQueryService: SharingQueryResolver
) : SharingQueryService<Flux<SharingGroup>>,
        SharingCommandService<AddPermissionCommand, DataReference<String, Any>> {

    @PostMapping(RestPaths.startSharing)
    override fun startSharing(pc: AddPermissionCommand) {
        sharingCommandService.startSharing(pc)
    }

    @PostMapping(RestPaths.stopSharing)
    override fun stopSharing(dataReference: DataReference<String, Any>) {
        sharingCommandService.stopSharing(dataReference)
    }

    @PostMapping(RestPaths.fetchByDataReference)
    override fun fetchByDataReference(@RequestBody dataReference: DataReference<String, Any>): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.fetchByDataReference(dataReference))
    }

    @GetMapping(RestPaths.fetchByAuthorId)
    override fun fetchByAuthorId(@PathVariable authorId: String): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.fetchByAuthorId(authorId))
    }

    @GetMapping(RestPaths.ping)
    fun pingPong() = Mono.just("pong")
}

internal object RestPaths {
    const val startSharing = "/start-sharing"
    const val stopSharing = "/stop-sharing"
    const val fetchByDataReference = "/fetch-by-data-reference"
    const val fetchByAuthorId = "/fetch-by-author-id"
    const val ping = "/ping"
}