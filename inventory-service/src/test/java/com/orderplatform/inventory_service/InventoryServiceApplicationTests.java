package com.orderplatform.inventory_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"inventory.commands", "inventory.events"})
class InventoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
