package de.fraunhofer.isst.dataspaceconnector.camel.config.security;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Creates a user with the "ADMIN" role for Spring Security.
 */
@Configuration
@EnableWebSecurity
@NoArgsConstructor
public class MultipleEntryPointsSecurityConfig {

    /**
     * The username.
     */
    @Value("${spring.security.user.name}")
    private String username;

    /**
     * The password.
     */
    @Value("${spring.security.user.password}")
    private String password;

    /**
     * Sets up a default admin user.
     *
     * @return the user details manager the admin user has been added to.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        final var manager = new InMemoryUserDetailsManager();
        manager.createUser(User
                .withUsername(username)
                .password(encoder().encode(password))
                .roles("ADMIN").build());
        return manager;
    }

    /**
     * Bean providing a password encoder.
     *
     * @return the password encoder.
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}
