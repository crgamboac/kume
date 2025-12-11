package com.kume.kume.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CspNonceFilterTest {

    @Test
    void testDoFilterInternal() throws ServletException, IOException {
        CspNonceFilter filter = new CspNonceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String nonce = (String) request.getAttribute("cspNonce");
        assertNotNull(nonce, "El atributo cspNonce no debería ser nulo");
        assertFalse(nonce.isEmpty(), "El nonce no debería estar vacío");

        String cspHeader = response.getHeader("Content-Security-Policy");
        assertNotNull(cspHeader, "El header Content-Security-Policy debería existir");

        assertTrue(cspHeader.contains("default-src 'self'"));
        assertTrue(cspHeader.contains("script-src 'self' https://cdn.jsdelivr.net 'nonce-" + nonce + "'"));
        assertTrue(cspHeader.contains("style-src 'self' 'unsafe-inline'"));

        verify(filterChain).doFilter(request, response);
    }
}