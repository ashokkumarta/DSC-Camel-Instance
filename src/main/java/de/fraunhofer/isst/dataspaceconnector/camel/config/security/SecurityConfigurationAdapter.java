package de.fraunhofer.isst.dataspaceconnector.camel.config.security;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * This class takes care of the security configuration so that only users with the "ADMIN" role can
 * access endpoints behinde "/api".
 */
@Configuration
@NoArgsConstructor
public class SecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Override
    public final void configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .formLogin().disable()
                .antMatcher("/api/**")
                .authorizeRequests().anyRequest().hasRole("ADMIN")
                .and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint());
        httpSecurity.headers().frameOptions().disable();
    }

    /**
     * Bean providing an entry point to the admin realm.
     *
     * @return the entry point.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        final var entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("admin realm");
        return entryPoint;
    }

}
