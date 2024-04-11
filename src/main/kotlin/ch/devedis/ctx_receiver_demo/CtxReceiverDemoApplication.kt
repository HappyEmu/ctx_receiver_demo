package ch.devedis.ctx_receiver_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@SpringBootApplication
class CtxReceiverDemoApplication

fun main(args: Array<String>) {
    runApplication<CtxReceiverDemoApplication>(*args)
}

class AnimalDto(
    val id: String,
    val name: String,
)

class CreateAnimalDto(
    val name: String,
)

@RestController
@RequestMapping("/animals")
class AnimalController {
    val animals = mutableListOf(
        AnimalDto("1", "Dog"),
        AnimalDto("2", "Cat")
    )

    @GetMapping
    fun getAnimals(): ResponseEntity<List<AnimalDto>> {
        return ResponseEntity.ok(animals)
    }

    @GetMapping("/{id}")
    fun getAnimal(@PathVariable id: String): ResponseEntity<AnimalDto> {
        return ResponseEntity.ok(
            animals.find { it.id == id } ?: throw IllegalArgumentException("Animal not found")
        )
    }

    @PostMapping
    fun createAnimal(@RequestBody createAnimalDto: CreateAnimalDto): ResponseEntity<AnimalDto> {
        val animal = AnimalDto((this.animals.count() + 1).toString(), createAnimalDto.name)

        animals += animal

        return ResponseEntity.ok(
            animal
        )
    }
}
