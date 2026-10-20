package com.zycus.stockpulse;

import com.zycus.stockpulse.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StockPulseApplication implements CommandLineRunner {

	@Autowired
	private ProductService productService;

	public static void main(String[] args) {
		SpringApplication.run(StockPulseApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize seed data
		productService.initializeSeedData();
		System.out.println("StockPulse application started with seed data!");
	}
}