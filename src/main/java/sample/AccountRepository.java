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

    Mono<Integer> charge(Long id, Long amount) {
        String q = "UPDATE accounts SET balance = balance - :amount WHERE id = :id";
        return databaseClient.sql(q)
                .bind("id", id)
                .bind("amount", amount)
                .fetch()
                .rowsUpdated();
    }

}