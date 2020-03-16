package io.layer.spreadsheet.sharing

import io.layer.spreadsheet.sharing.component.UserIdResolver
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

class UserIdResolverTest {

    private val emails = hashSetOf(
            "john@smith.com",
            "john.smith@gmail.com",
            "foo@bar.com"
    )

    @Test
    fun testUserIdGenResolve() {
        val userIdResolver = UserIdResolver()

        //Collect a list of new IDs
        val ids = emails.map {
            userIdResolver.getUserId(it)
        }.toSet()

        //Shouldn't duplicate
        ids.size shouldBe emails.size

        //Check return of existing IDs
        emails.forEach { email ->
            val userId = userIdResolver.getUserId(email)
            ids shouldContain userId
        }


    }

}