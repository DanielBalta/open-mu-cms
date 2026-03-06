package io.github.felipeemerson.openmuapi;

import io.github.felipeemerson.openmuapi.configuration.PreDBConfigListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication()
public class OpenmuApiApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication app = new SpringApplication(OpenmuApiApplication.class);
		app.addListeners(new PreDBConfigListener());
		app.run(args);
	}

}
