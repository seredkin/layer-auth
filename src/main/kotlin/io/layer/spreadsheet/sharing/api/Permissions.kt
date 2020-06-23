package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator

interface Permission {
    val read: Boolean
    val write: Boolean
    val share: Boolean

    companion object {
        val READ: Permission = ReadPermission(read = true, write = false, share = false)
        val WRITE: Permission = WritePermission(read = true, write = true, share = false)
        val SHARE: Permission = SharePermission(read = true, write = true, share = true)
        @JsonCreator
        @JvmStatic
        fun of(read: Boolean, write: Boolean, share: Boolean): Permission = when {
            share && write && read -> SHARE
            write && read -> WRITE
            read -> READ
            else -> error("Wrong permission values:$read, write:$write, share:$share")
        }
    }
}

internal class ReadPermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission

internal class WritePermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission

internal class SharePermission(
        override val read: Boolean,
        override val write: Boolean,
        override val share: Boolean) : Permission


