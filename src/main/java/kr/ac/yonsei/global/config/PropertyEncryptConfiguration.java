package kr.ac.yonsei.global.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableEncryptableProperties
public class PropertyEncryptConfiguration {
    @Autowired
    private Environment environment;

    /**
     * Create Encryptor Object
     * @return encryptor object
     */
    @Bean("stringEncryptor")
    public PooledPBEStringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPoolSize(2);
        encryptor.setPassword(environment.getProperty("jasypt.encryptor.password", "NONE"));
        encryptor.setAlgorithm("PBEWithSHA256And128BitAES-CBC-BC");
        return encryptor;
    }
}