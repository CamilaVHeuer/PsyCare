package com.camicompany.PsyCare;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
class PsyCareApplicationTests {
	@Autowired
	Environment env;

	@Test
	void contextLoads() {
	}

	@Test
	void debugProfiles() {
		System.out.println(Arrays.toString(env.getActiveProfiles()));
	}

}
