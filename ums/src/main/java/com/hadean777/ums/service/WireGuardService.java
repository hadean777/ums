package com.hadean777.ums.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.Base64;

@Service
public class WireGuardService {

    @Value("${wg.server.endpoint}")
    private String serverEndpoint;

    @Value("${wg.server.public-key}")
    private String serverPublicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static class WireGuardKeyPair {
        private final String publicKey;
        private final String privateKey;

        public WireGuardKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }

    public WireGuardKeyPair generateKeyPair() throws Exception {
        // Using BouncyCastle direct generators for raw X25519 keys
        org.bouncycastle.crypto.generators.X25519KeyPairGenerator generator = new org.bouncycastle.crypto.generators.X25519KeyPairGenerator();
        generator.init(new org.bouncycastle.crypto.params.X25519KeyGenerationParameters(new java.security.SecureRandom()));
        org.bouncycastle.crypto.AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

        org.bouncycastle.crypto.params.X25519PublicKeyParameters publicKeyParams = (org.bouncycastle.crypto.params.X25519PublicKeyParameters) keyPair.getPublic();
        org.bouncycastle.crypto.params.X25519PrivateKeyParameters privateKeyParams = (org.bouncycastle.crypto.params.X25519PrivateKeyParameters) keyPair.getPrivate();

        String publicKey = Base64.getEncoder().encodeToString(publicKeyParams.getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(privateKeyParams.getEncoded());

        return new WireGuardKeyPair(publicKey, privateKey);
    }

    public String getServerEndpoint() {
        return serverEndpoint;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }
}
