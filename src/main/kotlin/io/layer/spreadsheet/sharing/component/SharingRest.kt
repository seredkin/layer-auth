package io.layer.spreadsheet.sharing.component

import io.layer.spreadsheet.sharing.api.AddPermissionCommand
import io.layer.spreadsheet.sharing.api.DataRange
import io.layer.spreadsheet.sharing.api.DataReference
import io.layer.spreadsheet.sharing.api.SharingCommandService
import io.layer.spreadsheet.sharing.api.SharingGroup
import io.layer.spreadsheet.sharing.api.SharingQueryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Stream

@RestController("/")
class SharingRest(
        private val sharingCommandService: SharingCommandResolver,
        private val sharingQueryService: SharingQueryResolver
) : SharingQueryService<Flux<SharingGroup>>,
        SharingCommandService<AddPermissionCommand, DataReference<String, Any>> {

    @PostMapping(RestPaths.startSharing)
    override fun startSharing(@RequestBody pc: AddPermissionCommand) {
        sharingCommandService.startSharing(pc)
    }

    @PostMapping(RestPaths.stopSharing)
    override fun stopSharing(@RequestBody dataReference: DataReference<String, Any>) {
        sharingCommandService.stopSharing(dataReference)
    }

    @PostMapping(RestPaths.fetchByDataReference)
    override fun fetchByDataReference(@RequestBody dataReference: DataReference<String, DataRange?>): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.fetchByDataReference(dataReference))
    }

    @GetMapping(RestPaths.fetchByAuthorId)
    override fun fetchByAuthorId(@PathVariable authorId: String): Flux<SharingGroup> {
        return Flux.fromStream(sharingQueryService.fetchByAuthorId(authorId))
    }

    @GetMapping(RestPaths.ping)
    fun pingPong() = Mono.just("pong")

    override fun fetchBySharingGroupId(sharingGroupId: String): Stream<SharingGroup> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

internal object RestPaths {
    const val startSharing = "/start-sharing"
    const val stopSharing = "/stop-sharing"
    const val fetchByDataReference = "/fetch-by-data-reference"
    const val fetchByAuthorId = "/fetch-by-author-id"
    const val ping = "/ping"
}