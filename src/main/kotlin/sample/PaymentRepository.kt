package sample

import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.sql.Timestamp

@Component
class PaymentRepository(
  private val databaseClient: DatabaseClient
) {

  fun save(payment: Payment): Mono<Int> {
    val q = "INSERT INTO payments (id, account_id, amount) VALUES ($1, $2, $3)"
    return databaseClient.execute(q)
      .bind(0, payment.id)
      .bind(1, payment.accountId)
      .bind(2, payment.amount)
      .fetch()
      .rowsUpdated()
  }
}