package com.gsr;

import com.gsr.service.KrakenWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableKafka
public class GsrCandleAggregatorHarunApplication implements CommandLineRunner {

	private final KrakenWebSocketService krakenWebSocketService;

	public GsrCandleAggregatorHarunApplication(KrakenWebSocketService krakenWebSocketService) {
		this.krakenWebSocketService = krakenWebSocketService;
	}

	public static void main(String[] args) {
		SpringApplication.run(GsrCandleAggregatorHarunApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		krakenWebSocketService.connect();
	}

}
