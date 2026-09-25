package fr.simplex_software.workshop.security.conformance;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import io.smallrye.jwt.util.*;

import java.security.*;
import java.time.*;
import java.util.*;

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
