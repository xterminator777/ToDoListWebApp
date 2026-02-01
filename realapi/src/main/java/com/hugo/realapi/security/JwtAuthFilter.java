package com.hugo.realapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;

    public JwtAuthFilter(JwtUtil jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // Let CORS preflight requests through
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        if (path.startsWith("/api/auth") || path.equals("/api/health")) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Missing Bearer token\"}");
            return;
        }

        String token = auth.substring("Bearer ".length());
        try {
            JwtUtil.JwtClaims claims = jwt.verifyAndParse(token);

            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.userId(), null, List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(req, res);
        } catch (Exception e) {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Invalid token\"}");
        }
    }
}
