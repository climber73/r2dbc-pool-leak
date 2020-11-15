package sample

import java.time.Instant
import java.util.*

data class CreatePaymentRequest(
  val reserveId: UUID,
  val quantity: Long,
  val gtin: String,
  val paymentType: Int,
  val instant: Instant = Instant.now()
)