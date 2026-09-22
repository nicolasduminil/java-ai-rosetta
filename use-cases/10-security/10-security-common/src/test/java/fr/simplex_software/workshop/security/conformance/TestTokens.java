package fr.simplex_software.workshop.security.conformance;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import io.smallrye.jwt.util.*;

import java.security.*;
import java.time.*;
import java.util.*;

/**
 * Mints RS256 JWTs for the conformance test, signed with the test-only private key
 * ({@code src/test/resources/keys/privateKey.pem}). The three applications verify these tokens with the
 * public key shipped in the common module ({@code src/main/resources/keys/publicKey.pem}), so the same
 * token is accepted by the Spring Security resource server and by Quarkus MP JWT alike.
 *
 * <p>The {@code groups} claim carries the roles (MP JWT maps it to roles directly; the Spring stacks map it
 * to {@code ROLE_}-prefixed authorities), and {@code sub}/{@code upn} carry the username that the per-user
 * vector-store filter keys on. The private key is read with smallrye's {@code KeyUtils} (JDK 21 has no
 * built-in PEM reader); Nimbus does the signing (it is already on both Spring stacks via the resource server).
 */
public final class TestTokens
{
  public static final String ISSUER = "https://rosetta.security.test";

  private static final PrivateKey PRIVATE_KEY = loadSigningKey();

  private TestTokens()
  {
  }

  public static String alice()
  {
    return tokenFor("alice", "USER");
  }

  public static String bob()
  {
    return tokenFor("bob", "USER");
  }

  public static String admin()
  {
    return tokenFor("admin", "USER", "ADMIN");
  }

  public static String tokenFor(String username, String... roles)
  {
    try
    {
      Instant now = Instant.now();
      var signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),
        new JWTClaimsSet.Builder()
          .issuer(ISSUER)
          .subject(username)
          .claim("upn", username)
          .claim("preferred_username", username)
          .claim("groups", Arrays.asList(roles))
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plus(Duration.ofHours(1))))
          .build());
      signedJwt.sign(new RSASSASigner(PRIVATE_KEY));
      return signedJwt.serialize();
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not mint a test token for " + username, e);
    }
  }

  private static PrivateKey loadSigningKey()
  {
    try
    {
      return KeyUtils.readPrivateKey("/keys/privateKey.pem");
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not load the test signing key /keys/privateKey.pem", e);
    }
  }
}
