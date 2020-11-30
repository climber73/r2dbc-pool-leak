package sample;

import io.r2dbc.spi.R2dbcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@RestController
class TestController {

    final static int MAX_RETRIES = 10;
    final static Logger log = LoggerFactory.getLogger(TestController.class);

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionalOperator txOperator;

    public TestController(AccountRepository accountRepository,
                          PaymentRepository paymentRepository,
                          TransactionalOperator txOperator) {
        this.accountRepository = accountRepository;
        this.paymentRepository = paymentRepository;
        this.txOperator = txOperator;
    }

    @PostMapping("/payment")
    Mono<String> makePayment() {
        long accountId = 1;
        long amount = 1;
        UUID paymentId = UUID.randomUUID();
        log.debug("got request [{}]", paymentId);
        var payment = new Payment(paymentId, accountId, amount);
        return executeInTransactionWithRetries(
                accountRepository.findById(accountId)
                        .flatMap(account -> {
                            if (account.balance - amount < 0) {
                                return Mono.error(
                                        new RuntimeException("account [$accountId] has not enough money"));
                            } else {
                                return accountRepository
                                        .updateBalance(accountId, account.balance - amount);
                            }
                        })
                        .then(paymentRepository.save(payment)))
                .then(
                        Mono.fromCallable(() -> { // todo!
                            log.debug("payment [{}] created", paymentId);
                            return "OK";
                        })
                );
    }

    Mono<?> executeInTransactionWithRetries(Mono<?> mono) {
        UUID uuid = UUID.randomUUID();
        return mono
                .as(txOperator::transactional)
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, Duration.ZERO)
                        .filter(it -> it instanceof DataAccessException)
                        .filter(it -> {
                            R2dbcException cause = (R2dbcException) it.getCause();
                            // don't know why io.r2dbc.postgresql.ExceptionFactory
                            // does not recognize 40001 state:
                            return "40001".equals(cause.getSqlState());
                        })
                        .doBeforeRetry(s -> log.debug("retry for {}", uuid)))
                .onErrorResume(e -> Mono.fromRunnable(() -> log.debug("tx for [{}] has not succeed within 10 retries", uuid))
                        .then(Mono.empty()))
                .doOnCancel(() -> log.debug("got cancel signal for [{}]", uuid));
    }
}