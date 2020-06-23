package io.layer.spreadsheet.sharing.api

import com.fasterxml.jackson.annotation.JsonCreator

/** Abstract definition of the shareable data
 * @see FileReference
 * @see SheetReference
 * @see RangeReference
 * */
interface DataReference<S, out D> {
    val id: S?
    val fileId: S
    val sheetId: S?
    val range: D?

    fun type(): String
    companion object {
        const val TYPE_FILE = "FILE"
        const val TYPE_SHEET = "SHEET"
        const val TYPE_CELL_SET = "CELL_SET"
        const val TYPE_CELL_RANGE = "CELL_RANGE"
        @JsonCreator
        @JvmStatic
        fun creator(fileId: String, sheetId: String?, range: DataRange?): DataReference<String, DataRange?> = when {
            range == null && sheetId == null -> FileReference(fileId = fileId)
            range != null && sheetId != null -> RangeReference(fileId = fileId, sheetId = sheetId, range = range)
            sheetId != null -> SheetReference(fileId = fileId, sheetId = sheetId)
            else -> error("Range cannot be referenced without a sheetId: $fileId, $sheetId, $range")
        }
    }
}
data class FileReference(
        override val id: String?=null,
        override val fileId: String,
        override val sheetId: String? = null,
        override val range: DataRange? = null)
    : DataReference<String, DataRange> {
    override fun type() = DataReference.TYPE_FILE
}

data class SheetReference(
        override val id: String? = null,
        override val fileId: String,
        override val sheetId: String,
        override val range: DataRange? = null)
    : DataReference<String, DataRange> {
    override fun type() = DataReference.TYPE_SHEET
}

data class RangeReference(
        override val id: String? = null,
        override val fileId: String,
        override val sheetId: String,
        override val range: DataRange)
    : DataReference<String, DataRange> {
    override fun type() = range.type()

}

