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
        UserDetails DrSarahJohnson = User.builder()
                .username("Dr. Sarah Johnson")
                .password("{noop}sarah123")
                .roles("EMPLOYEE", "DOCTOR")
                .build();

        UserDetails DrMichaelChen = User.builder()
                .username("Dr. Michael Chen")
                .password("{noop}chen123")
                .roles("EMPLOYEE", "MANAGER", "DOCTOR")
                .build();

        UserDetails DrEmilyDavis = User.builder()
                .username("Dr. Emily Davis")
                .password("{noop}davis123")
                .roles("DOCTOR")
                .build();

        UserDetails DrRobertBrown = User.builder()
                .username("Dr. Robert Brown")
                .password("{noop}brown123")
                .roles("DOCTOR")
                .build();

        UserDetails DrLisaMartinez = User.builder()
                .username("Dr. Lisa Martinez")
                .password("{noop}lisa123")
                .roles("DOCTOR")
                .build();

        UserDetails DrJamesWilson = User.builder()
                .username("Dr. James Wilson")
                .password("{noop}james123")
                .roles("DOCTOR")
                .build();

        UserDetails DrShekhar = User.builder()
                .username("Dr. Shekhar")
                .password("{noop}shekhar123")
                .roles("DOCTOR")
                .build();

        return new InMemoryUserDetailsManager(
                DrSarahJohnson, DrMichaelChen, DrEmilyDavis,
                DrRobertBrown, DrLisaMartinez, DrJamesWilson, DrShekhar
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
