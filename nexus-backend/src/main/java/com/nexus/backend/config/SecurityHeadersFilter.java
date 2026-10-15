package com.nexus.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpRes) {
            httpRes.setHeader("X-Content-Type-Options", "nosniff");
            httpRes.setHeader("X-Frame-Options", "DENY");
            httpRes.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpRes.setHeader("Permissions-Policy", "camera=(), microphone=(self), geolocation=()");
            httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            httpRes.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            httpRes.setHeader("Cross-Origin-Resource-Policy", "same-origin");
            httpRes.setHeader("Content-Security-Policy",
                    "default-src 'self'; connect-src 'self' https: http: ws: wss: blob:; font-src 'self' https://fonts.gstatic.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; script-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; media-src 'self' data: blob:; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none';");
            
            if (request instanceof HttpServletRequest httpReq) {
                String origin = httpReq.getHeader("Origin");
                if (origin != null && !origin.isBlank()) {
                    httpRes.setHeader("Access-Control-Allow-Origin", origin);
                    httpRes.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, Upgrade, Authorization, X-Requested-With");
                    httpRes.setHeader("Access-Control-Max-Age", "86400");
                    httpRes.setHeader("Vary", "Origin");
                }
            }
        }
        chain.doFilter(request, response);
    }
}
