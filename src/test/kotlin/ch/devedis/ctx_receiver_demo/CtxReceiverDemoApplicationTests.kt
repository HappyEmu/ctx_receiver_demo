package ch.devedis.ctx_receiver_demo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

import strikt.api.expectThat
import strikt.assertions.isEqualTo

@SpringBootTest
@ActiveProfiles("integrationtest")
@AutoConfigureMockMvc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CustomAppTest

interface UnauthenticatedApi {
    fun login(user: String): AuthenticatedUserContext
}

interface MyApi {
    fun getAnimal(id: String): AnimalDto
    fun createAnimal(createAnimalDto: CreateAnimalDto): AnimalDto
}

@CustomAppTest
class CtxReceiverDemoApplicationTests @Autowired constructor(
    override val mockMvc: MockMvc,
    override val objectMapper: ObjectMapper,
) : MyIntegrationTestScope {

    @Test
    fun testCreateAnimal() = integrationTest {
        val user = login("User")

        asUser(user) {
            val animal = createAnimal(CreateAnimalDto(name = "Pluto"))
            expectThat(animal.name).isEqualTo("Pluto")
        }
    }

    @Test
    fun testGetAnimal() = integrationTest {
        val user = login("Admin")

        asUser(user) {
            val animal = createAnimal(CreateAnimalDto(name = "Pluto"))
            expectThat(animal.name).isEqualTo("Pluto")

            val fetchedAnimal = getAnimal(animal.id)
            expectThat(fetchedAnimal.name).isEqualTo("Pluto")
        }
    }
}

interface MyIntegrationTestScope {
    val mockMvc: MockMvc
    val objectMapper: ObjectMapper

    fun integrationTest(body: context(MockMvc, ObjectMapper, UnauthenticatedApi) () -> Unit) {
        body(mockMvc, objectMapper, UnauthenticatedApiImpl())
    }

    context(UnauthenticatedApi)
    fun <R> asUser(
        user: AuthenticatedUserContext,
        body: context(MockMvc, ObjectMapper, MyApi, AuthenticatedUserContext) () -> R,
    ): R {
        with(user) {
            with(mockMvc) {
                with(objectMapper) {
                    return body(mockMvc, objectMapper, MyApiImpl(), user)
                }
            }
        }
    }
}

context(MockMvc, ObjectMapper, AuthenticatedUserContext)
class MyApiImpl : MyApi {
    override fun getAnimal(id: String): AnimalDto {
        return get("/animals/$id") { authenticated() }.andReturnAs()
    }

    override fun createAnimal(createAnimalDto: CreateAnimalDto): AnimalDto {
        return post("/animals") {
            authenticated()
            writeJsonContent(createAnimalDto)
        }.andReturnAs()
    }
}

typealias IdToken = String

interface AuthenticatedUserContext {
    val idToken: IdToken
}

class UnauthenticatedApiImpl : UnauthenticatedApi {
    override fun login(user: String): AuthenticatedUserContext {
        return object : AuthenticatedUserContext {
            override val idToken = user
        }
    }
}

context(AuthenticatedUserContext)
fun MockHttpServletRequestDsl.authenticated() {
    header("Authorization", "Bearer $idToken")
}

context(ObjectMapper)
fun <T> MockHttpServletRequestDsl.writeJsonContent(jsonContent: T) {
    contentType = MediaType.APPLICATION_JSON
    content = writeValueAsString(jsonContent)
}

context (ObjectMapper)
inline fun <reified T> ResultActionsDsl.andReturnAs(): T {
    val ref: TypeReference<T> = object : TypeReference<T>() {}
    val result = andReturn()

    return readValue(result.response.contentAsString, ref)
}
