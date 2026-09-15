package com.logitracker.trackingservice;

import org.springframework.boot.SpringApplication;

public class TestTrackingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TrackingServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
