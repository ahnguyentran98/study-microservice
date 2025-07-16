package com.microservices.userservice.config;

import com.microservices.userservice.security.AuthTokenFilter;
import com.microservices.userservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    
    private final UserService userService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final PasswordEncoder passwordEncoder;
    
    public WebSecurityConfig(UserService userService, AuthEntryPointJwt unauthorizedHandler, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.passwordEncoder = passwordEncoder;
    }
    
    // JWT filter removed - API Gateway handles authorization
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/users/register").permitAll()
                    .requestMatchers("/api/users/login").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().permitAll()  // Allow all requests - API Gateway handles authorization
            );
        
        http.authenticationProvider(authenticationProvider());
        // JWT filter removed - API Gateway handles authorization
        
        return http.build();
    }
}