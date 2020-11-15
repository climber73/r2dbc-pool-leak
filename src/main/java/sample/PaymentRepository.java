package sample;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class PaymentRepository {

    final private DatabaseClient databaseClient;

    public PaymentRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    Mono<Integer> save(Payment payment) {
        String q = "INSERT INTO payments (id, account_id, amount) VALUES ($1, $2, $3)";
        return databaseClient.sql(q)
                .bind(0, payment.id)
                .bind(1, payment.accountId)
                .bind(2, payment.amount)
                .fetch()
                .rowsUpdated();
    }
}