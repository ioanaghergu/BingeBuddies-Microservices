package org.market.movieservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/movies/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/movies/{movieId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/{reviewId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/{reviewId}").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverterBean())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverterBean() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();

            if (jwt.hasClaim("realm_access")) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                Object rolesObject = realmAccess.get("roles");
                if (rolesObject instanceof Collection) {
                    Collection<String> realmRoles = (Collection<String>) rolesObject;
                    authorities.addAll(realmRoles.stream()
                            .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                            .collect(Collectors.toList()));
                }
            }
            log.info("Authorities extracted from JWT for MOVIESERVICE: {}", authorities);
            return authorities;
        });
        return converter;
    }
}
