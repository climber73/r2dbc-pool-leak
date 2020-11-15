package sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Configuration
class DatabaseConfiguration {
    @Bean
    @Primary
    TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        DefaultTransactionDefinition td = new DefaultTransactionDefinition();
        td.setIsolationLevelName("ISOLATION_SERIALIZABLE");
        return TransactionalOperator.create(transactionManager, td);
    }
}