package fr.simplex_software.workshop.security.langchain4j.boot;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.*;
import org.springframework.security.converter.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.*;

import java.security.interfaces.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig
{
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http)
  {
    return http
      .csrf(AbstractHttpConfigurer::disable)
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
   * Verifies the token with the shared public key and validates the issuer (in
   * public-key mode Spring would otherwise check only the signature and expiry).
   */
  @Bean
  JwtDecoder jwtDecoder(
    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") Resource publicKey)
    throws Exception
  {
    var jwtDecoder = NimbusJwtDecoder.withPublicKey(RsaKeyConverters.x509()
      .convert(publicKey.getInputStream())).build();
    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://rosetta.security.test"));
    return jwtDecoder;
  }
}
