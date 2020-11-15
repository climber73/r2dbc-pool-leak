package sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionalOperator txOperator;

    @PostMapping("/payment")
    Mono<String> makePayment() {
        long accountId = 1;
        long amount = 1;
        UUID paymentId = UUID.randomUUID();
        log.info("got payment request [{}]", paymentId);
        var payment = new Payment(paymentId, accountId, amount);
        try {
            return executeWithRetries(
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
                                log.info("payment [{}] created", paymentId);
                                return "OK";
                            })
                    );

        } catch (RetryAttemptsExhaustedException e) {
            log.info(e.getMessage());
            return Mono.error(e);
        }
    }

    Mono<?> executeWithRetries(Mono<?> mono) {
        UUID uuid = UUID.randomUUID();
        return mono
                .as(txOperator::transactional)
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, Duration.ZERO)
                        .filter(it -> it instanceof ConcurrencyFailureException)
                        .doBeforeRetry(s -> log.info("retry for {}", uuid)))
                .onErrorMap(ConcurrencyFailureException.class,
                        e -> new RetryAttemptsExhaustedException("tx has not succeed within 10 retries"));
    }
}