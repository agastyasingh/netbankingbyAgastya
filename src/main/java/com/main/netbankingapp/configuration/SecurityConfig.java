package com.main.netbankingapp.configuration;

import com.main.netbankingapp.service.CustomOidcUserService;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
//	@Value("${app.frontend.url}")
//	private String frontendUrl;

    @Autowired
    private CustomOidcUserService customOidcUserService;
    
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/accounts/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/oauth2/**", "/login/**", "/api/user/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)  // Use custom handler
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
            )
            .logout(logout -> logout
            	    .logoutUrl("/logout")  // ← Endpoint matches Angular call
            	    .logoutSuccessHandler((request, response, authentication) -> {
            	        // Clear session
            	        request.getSession().invalidate();
            	        
            	        // Set CORS headers
//            	        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
            	        response.setHeader("Access-Control-Allow-Origin", "https://lightyellow-jaguar-749540.hostingersite.com");
            	        response.setHeader("Access-Control-Allow-Credentials", "true");
            	        
            	        // Return success response (don't redirect)
            	        response.setStatus(HttpServletResponse.SC_OK);
            	        response.setContentType("application/json");
            	        response.getWriter().write("{\"message\": \"Logged out successfully\"}");
            	    })
            	    .permitAll()
            	);

//            .logout(logout -> logout
//                .logoutSuccessUrl("http://localhost:4200/login?logout")
//                .permitAll()
//            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://lightyellow-jaguar-749540.hostingersite.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "JSESSIONID"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
