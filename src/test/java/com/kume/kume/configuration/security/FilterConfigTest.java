package com.kume.kume.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FilterConfigTest {

    @Test
    void testCspReg() {
        FilterConfig config = new FilterConfig();
        CspNonceFilter mockFilter = mock(CspNonceFilter.class);

        FilterRegistrationBean<CspNonceFilter> registration = config.cspReg(mockFilter);

        assertNotNull(registration);
        assertEquals(mockFilter, registration.getFilter());
        assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
    }
}
