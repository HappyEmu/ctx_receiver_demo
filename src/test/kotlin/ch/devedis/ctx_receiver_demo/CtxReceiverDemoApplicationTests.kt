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
        val animal = createAnimal(CreateAnimalDto(name = "Pluto"))
        expectThat(animal.name).isEqualTo("Pluto")
    }

    @Test
    fun testGetAnimal() = integrationTest {
        val animal = createAnimal(CreateAnimalDto(name = "Pluto"))
        expectThat(animal.name).isEqualTo("Pluto")

        val fetchedAnimal = getAnimal(animal.id)
        expectThat(fetchedAnimal.name).isEqualTo("Pluto")
    }
}

interface MyIntegrationTestScope {
    val mockMvc: MockMvc
    val objectMapper: ObjectMapper

    fun integrationTest(body: MyApi.() -> Unit) {
        TODO()
    }
}
