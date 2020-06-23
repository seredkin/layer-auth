package io.layer.spreadsheet.sharing.api

interface SharingCommandService<Command, Reference>{
    fun startSharing(pc: Command)
    fun stopSharing(dataReference: Reference)
}