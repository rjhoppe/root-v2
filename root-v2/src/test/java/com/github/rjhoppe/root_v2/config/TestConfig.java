package com.github.rjhoppe.root_v2.config;

import com.github.rjhoppe.root_v2.utils.Wordnik;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Wordnik mockedWordnik() throws Exception {
        Wordnik mockWordnik = Mockito.mock(Wordnik.class);
        // Stub validate() method to do nothing, avoiding actual API calls
        Mockito.doNothing().when(mockWordnik).validate();
        // Stub validateWord() to return true for any word, for general test context loading
        Mockito.when(mockWordnik.validateWord(Mockito.anyString())).thenReturn(true);
        return mockWordnik;
    }
}
