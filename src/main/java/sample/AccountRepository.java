package sample;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class AccountRepository {

    final private DatabaseClient databaseClient;

    public AccountRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    Mono<Account> findById(Long id) {
        String q = "SELECT balance FROM accounts WHERE id = :id";
        return databaseClient.sql(q)
                .bind("id", id)
                .map(row -> new Account(id, row.get("balance", Long.class)))
                .one();
    }

    Mono<Integer> updateBalance(Long contractId, Long balance) {
        String q = "UPDATE accounts SET balance = :balance WHERE id = :id";
        return databaseClient.sql(q)
                .bind("id", contractId)
                .bind("balance", balance)
                .fetch()
                .rowsUpdated();
    }

    Mono<Integer> save(Account account) {
        String insert = "INSERT INTO accounts (id, balance) VALUES ($1, $2)";
        return databaseClient.sql(insert)
                .bind(0, account.id)
                .bind(1, account.balance)
                .fetch()
                .rowsUpdated();
    }

}