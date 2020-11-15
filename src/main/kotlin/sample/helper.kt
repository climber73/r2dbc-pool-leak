package sample

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.r2dbc.spi.Row
import kotlinx.coroutines.CancellationException
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

val registry = Metrics.globalRegistry
val retries = Counter.builder("billing.db.retries")
  .register(registry)

fun Row.getLong(name: String) = get(name, java.lang.Long::class.java)?.toLong()
fun Row.getLong(index: Int) = get(index, java.lang.Long::class.java)?.toLong()
fun Row.getInt(name: String) = get(name, java.lang.Integer::class.java)?.toInt()
fun Row.getInt(index: Int) = get(index, java.lang.Integer::class.java)?.toInt()
fun Row.getByte(name: String) = get(name, java.lang.Byte::class.java)?.toByte()
fun Row.getByte(index: Int) = get(index, java.lang.Byte::class.java)?.toByte()
fun Row.getString(name: String) = get(name, String::class.java)
fun Row.getString(index: Int) = get(index, String::class.java)
fun Row.getUUID(name: String) = get(name, UUID::class.java)
fun Row.getUUID(index: Int) = get(index, UUID::class.java)
fun Row.getInstant(name: String) = get(name, LocalDateTime::class.java)?.toInstant(ZoneOffset.UTC)
fun Row.getInstant(index: Int) = get(index, LocalDateTime::class.java)?.toInstant(ZoneOffset.UTC)
fun Row.getBoolean(name: String) = get(name, java.lang.Boolean::class.java)?.booleanValue()
fun Row.getBoolean(index: Int) = get(index, java.lang.Boolean::class.java)?.booleanValue()

fun <T : Any> TransactionalOperator.executeWithRetries(mono: Mono<T>): Mono<T> {
//  var i = 0
  val uuid = UUID.randomUUID()
//  while (i++ < MAX_RETRIES) {
//    try {

//      return this.execute { status ->
//        mono
//          .onErrorResume()
//          .onErrorResume { e -> status.setRollbackOnly() }
//      }.awaitSingleOrNull()

//      Mono.create<Unit> { sink ->
//        sink.error()
//      }

//      val future = mono.toFuture()

      return mono
//        .onErrorResume(CancellationException::class.java) { e -> println("<<<< ++++");Mono.error(e) }
        .`as`(this::transactional)
        .retryWhen(Retry.fixedDelay(10, Duration.ZERO)
          .filter { it is ConcurrencyFailureException }
          .doBeforeRetry { retries.increment(); println(">>>> retry $uuid") })
        .onErrorMap(ConcurrencyFailureException::class.java) {
          RetryAttemptsExhaustedException("tx has not succeed within 10 retries") // todo: check if it works
        }
//        .doOnError(CancellationException::class.java) { e ->
//          println(">>>>() ${e.message}, cause: ${e.cause}, ${e.stackTraceToString()}")
//        }
        .doFinally { st ->
          if (st == SignalType.CANCEL) {
            println(">>>> mono $uuid cancelled")
//            future.cancel(false)
          }
        }
//        .toFuture()
//        .join()
//        .awaitSingleOrNull()

//    } catch (e: ConcurrencyFailureException) {
//      retries.increment()
//      println(">>>> $i $uuid")
//      delay((2.0.pow(i) * 10).toLong())
//    } catch (e: CancellationException) {
//
//      println(">>>> ${e.message}, cause: ${e.cause}, ${e.stackTraceToString()}")
//      mono.
//      return Mono.error(e)
//      mono.error
//      return Mono.from(Mono.delay(Duration.ofMillis(1000)))
//        .then(Mono.empty())
//    }
//  }
//  throw RetryAttemptsExhaustedException("tx has not succeed within $i retries")
}

fun <T : Any> TransactionalOperator.executeWithRetriesR(mono: Mono<T>): Mono<T> {
  val uuid = UUID.randomUUID()
  try {

//      Mono.create { sink ->
//        sink.error()
//      }
    return mono
      .`as`(this::transactional)
      .retryWhen(Retry.fixedDelay(10, Duration.ZERO)
        .filter { it is ConcurrencyFailureException }
        .doBeforeRetry { retries.increment(); println(">>>> retry $uuid") })
      .onErrorMap(ConcurrencyFailureException::class.java) {
        RetryAttemptsExhaustedException("tx has not succeed within 10 retries") // todo: check if it works
      }

  } catch (e: CancellationException) {

    println(">>>> >>>> ${e.message}, cause: ${e.cause}, ${e.stackTraceToString()}")
    throw e
  }
}

const val MAX_RETRIES = 10
