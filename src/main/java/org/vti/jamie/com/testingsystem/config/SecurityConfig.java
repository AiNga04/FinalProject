package org.vti.jamie.com.testingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;

import java.util.List;

@Configuration
public class SecurityConfig {
    // 1. Password hashing
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 2. Load account from MySQL
    @Bean
    public UserDetailsService userDetailsService(IAccountRepository accountRepository){
        return username -> {
            var account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

            return User.withUsername(username)
                    .password(account.getPasswordHash())
                    .authorities("ACCOUNT_TYPE_" + account.getAccountType().name())
                    .build();
        };
    }

    // 3. Verify username/password
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder){
        var provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    // 4. API authorization
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ACCOUNT_TYPE_" + jwt.getClaim("accountType"))));

        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // API Health Check
                .requestMatchers("/api/v1/health")
                .permitAll()

                // Swagger UI
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**"
                )
                .permitAll()

                // Spring error endpoint
                .requestMatchers("/error")
                .permitAll()

                .requestMatchers("/api/v1/auth/login")
                .permitAll()

                // Admin only
                .requestMatchers(
                        "/api/v1/accounts",
                        "/api/v1/accounts/**",
                        "/api/v1/departments",
                        "/api/v1/departments/**"
                ).hasRole("ADMIN")

                // Everything else blocked until configured
                .requestMatchers("/api/v1/auth/me").authenticated()

                    // Protect all unfinished APIs
                .anyRequest()
                .denyAll()
            )
            .oauth2ResourceServer(oauth -> oauth
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))

            );
        return http.build();
    }
}
