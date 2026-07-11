package com.example.pl_connect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@EnableCaching 
public class PlConnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlConnectApplication.class, args);
	}
}
