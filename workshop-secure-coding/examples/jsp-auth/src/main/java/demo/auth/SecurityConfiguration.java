package demo.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http,
            final LoginFailureHandler loginFailureHandler) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(loginFailureHandler)
                        .defaultSuccessUrl("/", true))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername("jerry")
                .password(passwordEncoder.encode("testing"))
                .roles("WORKSHOP_USER")
                .build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
