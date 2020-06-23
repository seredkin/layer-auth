package io.layer.spreadsheet.sharing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration


@SpringBootApplication
@Configuration
class SharingMicroservice {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SharingMicroservice>(*args)
        }

    }

}

