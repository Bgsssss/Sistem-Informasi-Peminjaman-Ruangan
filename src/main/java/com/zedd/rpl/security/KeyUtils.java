package com.zedd.rpl.security;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

  private KeyUtils() {
  }

  public static PrivateKey loadPrivateKey(final Resource resource) throws Exception {
    final String key = read(resource)
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s+", "");
    final byte[] decoded = Base64.getDecoder().decode(key);
    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
  }

  public static PublicKey loadPublicKey(final Resource resource) throws Exception {
    final String key = read(resource)
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s+", "");
    final byte[] decoded = Base64.getDecoder().decode(key);
    return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
  }

  private static String read(final Resource resource) throws Exception {
    try (InputStream is = resource.getInputStream()) {
      return new String(is.readAllBytes());
    }
  }
}
