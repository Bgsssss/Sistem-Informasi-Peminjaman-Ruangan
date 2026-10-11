package com.zedd.rpl.config;

import com.zedd.rpl.security.KeyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class RsaKeyConfig {

    @Bean
    public PrivateKey privateKey() throws Exception {
        return KeyUtils.loadPrivateKey("keys/local-only/private_key.pem");
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        return KeyUtils.loadPublicKey("keys/local-only/public_key.pem");
    }
}
