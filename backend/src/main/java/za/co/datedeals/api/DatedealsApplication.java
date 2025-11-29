package za.co.datedeals.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DatedealsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatedealsApplication.class, args);
	}

}
