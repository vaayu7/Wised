package com.wised;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.wised.auth.repository", "com.wised.helpandsettings.repository", "com.wised.post.repository", "com.wised.people.repository", "com.wised.search.repository"})
@ComponentScan(basePackages = {"com.wised.auth", "com.wised.helpandsettings", "com.wised.post", "com.wised.people", "com.wised.bystream", "com.wised.home", "com.wised.search", "com.wised.notification"})
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
