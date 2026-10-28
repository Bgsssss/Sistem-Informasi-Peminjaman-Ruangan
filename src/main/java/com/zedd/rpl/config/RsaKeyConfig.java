package com.zedd.rpl.config;

import com.zedd.rpl.security.KeyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class RsaKeyConfig {

    @Bean
    public PrivateKey privateKey(
        @Value("${app.security.jwt.private-key-path:file:./keys/private_key.pem}")
        final Resource resource
    ) throws Exception {
        return KeyUtils.loadPrivateKey(resource);
    }

    @Bean
    public PublicKey publicKey(
        @Value("${app.security.jwt.public-key-path:file:./keys/public_key.pem}") final Resource resource) throws Exception {
        return KeyUtils.loadPublicKey(resource);
    }
}
