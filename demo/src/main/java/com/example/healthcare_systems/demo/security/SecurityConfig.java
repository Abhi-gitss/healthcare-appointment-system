package com.example.healthcare_systems.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {

        // Assign a common "DOCTOR" role for dashboard access
        UserDetails DrAditiSharma = User.builder()
                .username("Dr. Aditi Sharma")
                .password("{noop}aditi123")
                .roles("EMPLOYEE", "DOCTOR")
                .build();

        UserDetails DrRajeevKumar = User.builder()
                .username("Dr. Rajeev Kumar")
                .password("{noop}rajeev123")
                .roles("EMPLOYEE", "MANAGER", "DOCTOR")
                .build();

        UserDetails DrMeeraSingh = User.builder()
                .username("Dr. Meera Singh")
                .password("{noop}meera123")
                .roles("DOCTOR")
                .build();

        UserDetails DrSunilGrover = User.builder()
                .username("Dr. Sunil Grover")
                .password("{noop}sunil123")
                .roles("DOCTOR")
                .build();

        UserDetails DrPoojaChawla = User.builder()
                .username("Dr. Pooja Chawla")
                .password("{noop}pooja123")
                .roles("DOCTOR")
                .build();

        UserDetails DrAmitabhJoshi = User.builder()
                .username("Dr. Amitabh Joshi")
                .password("{noop}amitabh123")
                .roles("DOCTOR")
                .build();

        UserDetails DrShekhar = User.builder()
                .username("Dr. Shekhar")
                .password("{noop}shekhar123")
                .roles("DOCTOR")
                .build();

        return new InMemoryUserDetailsManager(
                DrAditiSharma, DrRajeevKumar, DrMeeraSingh,
                DrSunilGrover, DrPoojaChawla, DrAmitabhJoshi, DrShekhar
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (configure later for production)
                .authorizeHttpRequests(auth -> auth
                        // ✅ Public pages and static assets
                        .requestMatchers(
                                "/login",
                                "/", "/index.html", "/about.html", "/service.html",
                                "/department.html", "/department-single.html",
                                "/doctor.html", "/doctor-single.html",
                                "/blog-sidebar.html", "/blog-single.html",
                                "/contact.html", "/styleguide.html",
                                "/appoinment.html", "/patient-portal.html",
                                "/css/**", "/js/**", "/images/**", "/plugins/**"
                        ).permitAll()

                        // ✅ Allow public API endpoints for patient access
                        .requestMatchers(
                                "/api/patients/**",
                                "/api/appointments/**",
                                "/api/doctors/**"
                        ).permitAll()

                        // 🔒 Doctor dashboard only accessible to DOCTOR role
                        .requestMatchers("/doctor-dashboard").hasRole("DOCTOR")

                        // 🔒 All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // ✅ Login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/doctor-dashboard", true)
                        .permitAll()
                )

                // ✅ Logout config
                .logout(logout -> logout.permitAll())

                // ✅ Prevent HTML redirect for API requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                )

                .build();
    }
}
