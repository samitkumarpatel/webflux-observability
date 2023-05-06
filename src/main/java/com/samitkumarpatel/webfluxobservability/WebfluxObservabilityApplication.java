package com.samitkumarpatel.webfluxobservability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class WebfluxObservabilityApplication {
	final PersonRepository personRepository;
	public static void main(String[] args) {
		// This will enable spanId and traceId in the log in an spring webflux - This might not needed in upcoming spring releases.
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(WebfluxObservabilityApplication.class, args);
	}

	@Bean
	RouterFunction route() {
		return RouterFunctions
				.route()
				.GET("/person", this::getAll)
				.POST("/person", this::save)
				.build();
	}

	private Mono<ServerResponse> getAll(ServerRequest request) {
		log.info("person getAll");
		return ServerResponse.ok().body(personRepository.findAll(), Person.class);
	}

	private Mono<ServerResponse> save(ServerRequest request) {
		log.info("person save");
		return request
				.bodyToMono(Person.class)
				.flatMap(person -> personRepository.save(person))
				.flatMap(person -> ServerResponse.ok().body(Mono.just(person), Person.class));
	}
}

//Since Flyway does not work with R2DBC, we'll need to create the Flyway bean with the init method migrate(), which prompts Spring to run our migrations as soon as it creates the bean
@Configuration
@EnableConfigurationProperties({ R2dbcProperties.class, FlywayProperties.class })
class DatabaseConfig {
	@Bean(initMethod = "migrate")
	public Flyway flyway(FlywayProperties flywayProperties, R2dbcProperties r2dbcProperties) {
		return Flyway.configure()
				.dataSource(
						flywayProperties.getUrl(),
						r2dbcProperties.getUsername(),
						r2dbcProperties.getPassword()
				)
				/*
				.locations(flywayProperties.getLocations()
						.stream()
						.toArray(String[]::new))
				*/
				.baselineOnMigrate(true)
				.load();
	}
}

record Person(@Id Long id, String name, int age) {};

@Repository
interface PersonRepository extends R2dbcRepository<Person, Long> {}