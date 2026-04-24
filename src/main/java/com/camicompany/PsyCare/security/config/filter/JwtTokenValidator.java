package com.camicompany.PsyCare.security.config.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.camicompany.PsyCare.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;


    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jwtToken != null) {
            jwtToken = jwtToken.substring(7); //remove "Bearer "
            try {
                DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

                String username = jwtUtils.extractUsername(decodedJWT);

                String authorities = jwtUtils.getSpecificClaim(decodedJWT, "authorities").asString();


                Collection<? extends GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                SecurityContext context = SecurityContextHolder.getContext();

                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);

            } catch (JWTVerificationException e) {
                // Token present but invalid → 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {
                            "status": 401,
                            "error": "UNAUTHORIZED",
                            "message": "Invalid or expired token",
                            "timestamp": "%s"
                        }
                        """.formatted(LocalDateTime.now()));
                return;
            }
        }
        // No token → just continue without setting authentication (will be caught by Spring Security if endpoint is protected)
        filterChain.doFilter(request, response);
    }
}
