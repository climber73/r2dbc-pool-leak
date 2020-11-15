package sample

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.support.DefaultTransactionDefinition

@Configuration
class DatabaseConfiguration(
  private val r2dbcProps: R2dbcProperties
) {

  private val options = ConnectionFactoryBuilder.of(r2dbcProps) { null }.buildOptions()

  @Bean
  fun connectionFactory(): ConnectionFactory {
    val pgConnFactoryConfig = PostgresqlConnectionFactoryProvider.builder(options)
      .build()

    return pool(PostgresqlConnectionFactory(pgConnFactoryConfig), r2dbcProps.pool)
  }

  private fun pool(
    connectionFactory: ConnectionFactory,
    props: R2dbcProperties.Pool
  ): ConnectionPool {
    return ConnectionPool(
      ConnectionPoolConfiguration.builder(connectionFactory)
        .maxSize(props.maxSize)
        .initialSize(props.initialSize)
        .maxIdleTime(props.maxIdleTime)
        .validationQuery(props.validationQuery)
        .build()
    )
  }

  @Bean
  @Primary
  fun transactionalOperator(
    transactionManager: ReactiveTransactionManager): TransactionalOperator {
    val td = DefaultTransactionDefinition().also {
      it.setIsolationLevelName("ISOLATION_SERIALIZABLE")
    }
    return TransactionalOperator.create(transactionManager, td)
  }
}