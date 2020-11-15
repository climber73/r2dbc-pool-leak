package sample

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono

@Service
class TestService(
  private val accountRepository: AccountRepository,
  private val paymentRepository: PaymentRepository,
  private val txOperator: TransactionalOperator
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun makePayment(accountId: Long, request: CreatePaymentRequest): Mono<Payment> {
    logger.debug("account=[$accountId]: got [$request]")
    val now = request.instant
    val amount = 1L
    val payment = Payment(
      id = request.reserveId,
      accountId = accountId,
      amount = amount
    )
    return try {
      txOperator.executeWithRetries(
        accountRepository.findById(accountId)
          .flatMap { account ->
            if (account.balance - amount < 0) {
              Mono.error(
                RuntimeException("account [$accountId] has not enough money")
              )
            } else {
              accountRepository.updateBalance(accountId, account.balance - amount)
            }
          }
          .then(paymentRepository.save(payment))
      )
        .then(
          Mono.fromCallable {
            logger.debug("account [$accountId]: payment [${payment.id}] created")
            payment
          }
        )

    } catch (e: RetryAttemptsExhaustedException) {
      logger.debug(">>>> ${e.message}")
      Mono.error(e)
    }
  }

}