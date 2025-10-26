package com.togglr.security.filter;

import com.togglr.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                String subject = jwtService.extractUsername(token);
                String type = jwtService.extractType(token);

                List<SimpleGrantedAuthority> authorities;

                if ("client".equals(type)) {
                    String scopes = jwtService.extractScopes(token);
                    authorities = scopes != null ?
                            Arrays.stream(scopes.split(","))
                                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope.trim().toUpperCase()))
                                    .toList() : List.of();
                } else {
                    String roles = jwtService.extractRoles(token);
                    authorities = roles != null ?
                            Arrays.stream(roles.split(","))
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                                    .toList() : List.of();
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}