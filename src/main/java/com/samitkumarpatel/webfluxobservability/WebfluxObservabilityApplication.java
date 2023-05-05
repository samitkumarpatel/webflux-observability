package com.samitkumarpatel.webfluxobservability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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


record Person(Long id, String name, int age) {};

@Repository
interface PersonRepository extends R2dbcRepository<Person, Long> {}