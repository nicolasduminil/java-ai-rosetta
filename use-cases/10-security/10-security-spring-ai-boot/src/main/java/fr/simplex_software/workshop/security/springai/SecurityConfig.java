package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.tool.execution.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.*;
import org.springframework.security.converter.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.*;

import java.security.interfaces.*;

/**
 * A JWT bearer resource server. Every request must carry a token signed by the shared key; the {@code groups}
 * claim becomes {@code ROLE_}-prefixed authorities so method security ({@code @PreAuthorize}) can guard the
 * admin tool. The tool-execution exception processor is set to rethrow, so a denied tool call surfaces as an
 * error the controller turns into HTTP 403 rather than being swallowed and fed back to the model.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig
{
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
  {
    return http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
      .build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter()
  {
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("groups");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return converter;
  }

  /**
   * Verifies the token with the shared public key.
   */
  @Bean
  JwtDecoder jwtDecoder(
    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") Resource publicKey)
    throws Exception
  {
    var jwtDecoder = NimbusJwtDecoder.withPublicKey(RsaKeyConverters.x509().convert(publicKey.getInputStream())).build();
    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://rosetta.security.test"));
    return jwtDecoder;
  }

  @Bean
  ToolExecutionExceptionProcessor toolExecutionExceptionProcessor()
  {
    return DefaultToolExecutionExceptionProcessor.builder().alwaysThrow(true).build();
  }
}
