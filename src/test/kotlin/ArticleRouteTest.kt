package fr.dcproject

import fr.postgresjson.migration.Migrations
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(PER_CLASS)
class ArticleRouteTest: KoinTest {
    private val migrations: Migrations  by inject()

    @BeforeEach
    fun beforeAll() {
        startKoin {
            modules(Module)
            config.database = "test"
            config.username = "test"
            config.password = "test"

        }
        migrations.run()
    }

    @AfterEach
    fun afterAll() {
        migrations.forceAllDown()
        stopKoin()
    }

    private val article: String = """{
        "id" : "8e8dd0aa-2b2b-41e1-bff5-ea613c988774",
        "version_id" : "e3ec9ea8-87ac-46ac-8321-8f2bc8c687bc",
        "version_number" : 1,
        "title" : "title13",
        "annonymous" : false,
        "content" : "content13",
        "description" : "description13",
        "tags" : [ "sky", "nuclear" ],
        "created_at" : "2019-07-30T14:08:51.420Z",
        "created_by" : {
          "id" : "d821a211-10d6-4d65-b0db-e0bd33d21761",
          "name" : {
            "civility" : "m",
            "last_name" : "LAST NAME13",
            "first_name" : "first name13"
          },
          "birthday" : "1994-07-30",
          "user_id" : "127b9979-1474-4da1-8453-1e10462ae593",
          "vote_annonymous" : false,
          "follow_annonymous" : false,
          "user" : null,
          "created_at" : "2019-07-30T14:08:49.742Z"
        }
    }"""

    @Test
    fun testRoute() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Get, "/articles").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Post, "/articles") {
                this.setBody(article)
                this.addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/articles/8e8dd0aa-2b2b-41e1-bff5-ea613c988774").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("8e8dd0aa-2b2b-41e1-bff5-ea613c988774"))
            }
        }

    }
}
