package com.orderplatform.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"inventory.events","payment.events", "order.events"})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
