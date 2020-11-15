package sample

import java.util.*

data class Account(
  val id: Long,
  val balance: Long
)

data class Payment(
  val id: UUID,
  val accountId: Long,
  val amount: Long
)