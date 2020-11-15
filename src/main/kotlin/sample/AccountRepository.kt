package sample

import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AccountRepository(
  private val databaseClient: DatabaseClient
) {

  fun findById(id: Long): Mono<Account> {
    val q = "SELECT balance FROM accounts WHERE id = :id"
    return databaseClient.execute(q)
      .bind("id", id)
      .map { row ->
        Account(
          id = id,
          balance = row.getLong("balance")!!
        )
      }
      .one()
  }

  fun updateBalance(contractId: Long, balance: Long): Mono<Int> {
    val q = "UPDATE accounts SET balance = :balance WHERE id = :id"
    return databaseClient.execute(q)
      .bind("id", contractId)
      .bind("balance", balance)
      .fetch()
      .rowsUpdated()
  }

  suspend fun save(account: Account): Int {
    val insert =
      "INSERT INTO accounts (id, balance) VALUES ($1, $2)"
    return databaseClient.execute(insert)
        .bind(0, account.id)
        .bind(1, account.balance)
        .fetch()
        .awaitRowsUpdated()
  }

}