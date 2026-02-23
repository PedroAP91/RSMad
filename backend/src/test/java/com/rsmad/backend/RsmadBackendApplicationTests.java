package com.rsmad.backend;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.DockerClientFactory;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RsmadBackendApplicationTests {

	@BeforeAll
	static void requireDocker() {
		Assumptions.assumeTrue(
				DockerClientFactory.instance().isDockerAvailable(),
				"Docker no disponible, se omite RsmadBackendApplicationTests"
		);
	}

	@Test
	void contextLoads() {
	}

}
