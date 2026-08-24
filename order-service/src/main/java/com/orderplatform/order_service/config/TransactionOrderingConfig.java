package com.orderplatform.order_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(order = 0)
public class TransactionOrderingConfig {
}
