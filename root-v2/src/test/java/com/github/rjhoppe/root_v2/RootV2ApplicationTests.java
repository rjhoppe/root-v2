package com.github.rjhoppe.root_v2;

import com.github.rjhoppe.root_v2.utils.Wordnik;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
class RootV2ApplicationTests {

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public Wordnik wordnikClient() throws Exception {
			Wordnik mockWordnik = Mockito.mock(Wordnik.class);
			Mockito.doNothing().when(mockWordnik).validate();
			Mockito.when(mockWordnik.validateWord(Mockito.anyString())).thenReturn(true);
			return mockWordnik;
		}
	}

	@Test
	void contextLoads() {
	}
}