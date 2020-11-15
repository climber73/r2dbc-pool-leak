package sample

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
class TestController(
  private val testService: TestService
) {

  @PostMapping("/payment")
  fun createReserve(): Mono<String> {
    val request = CreatePaymentRequest(UUID.randomUUID(), 1, "1234", 1)
    return testService.makePayment(1, request)
      .map { "OK" }
  }

}